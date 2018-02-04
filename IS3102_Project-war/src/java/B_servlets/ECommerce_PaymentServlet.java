/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package B_servlets;

import HelperClasses.ShoppingCartLineItem;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.regex.Pattern;
import HelperClasses.Member;
import javax.ws.rs.core.Response;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Shizuka
 */
@WebServlet(name = "ECommerce_PaymentServlet", urlPatterns = {"/ECommerce_PaymentServlet"})
public class ECommerce_PaymentServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        HttpSession session = request.getSession();
        PrintWriter out = response.getWriter();
        
        try {
            String cardName = "";
            String creditNo = "";
            int securityCode;
            int month; 
            int year;
            double finalPrice = 0.0;
            long countryID = 0;
            long memberID = 0;
            ArrayList<ShoppingCartLineItem> shoppingCart = null;
            
            if(session.getAttribute("memberID") != null) {
                memberID = (long) session.getAttribute("memberID");
            } else {
                response.sendRedirect("/IS3102_Project-war/B/SG/shoppingCart.jsp"
                    + "?errMsg=Your session has expired, please login again.");
            }
            
//            if (session.getAttribute("countryID") != null) {
//                countryID = (long) session.getAttribute("countryID");
//            } else {
//                response.sendRedirect("/IS3102_Project-war/B/SG/shoppingCart.jsp"
//                    + "?errMsg=Your session has expired, please login again.");
//            }
            
            if((ArrayList<ShoppingCartLineItem>) session.getAttribute("shoppingCart") != null) {
                shoppingCart = (ArrayList<ShoppingCartLineItem>) session.getAttribute("shoppingCart");
                countryID = shoppingCart.get(0).getCountryID();
            } else {
                response.sendRedirect("/IS3102_Project-war/B/SG/shoppingCart.jsp"
                + "?errMsg=Invalid cart.");
            }
            
            if (!"".equals(request.getParameter("txtName")) &&
                    request.getParameter("txtName") != null) {
                cardName = request.getParameter("txtName");
            } else {
                response.sendRedirect("/IS3102_Project-war/B/SG/shoppingCart.jsp"
                    + "?errMsg=Please enter a valid name.");
            }
            
             if (!"".equals(request.getParameter("txtSecuritycode")) &&
                    isNumeric(request.getParameter("txtSecuritycode"))) {
                securityCode = Integer.parseInt(request.getParameter("txtSecuritycode"));
            } else {
                response.sendRedirect("/IS3102_Project-war/B/SG/shoppingCart.jsp"
                    + "?errMsg=Please enter a valid CVV/CVV2.");
            }
             
            if (isNumeric(request.getParameter("month"))) {
                month = Integer.parseInt(request.getParameter("month"));
                
                if (month < 0 || month > 12) {
                response.sendRedirect("/IS3102_Project-war/B/SG/shoppingCart.jsp"
                    + "?errMsg=Invalid Month inserted.");
                }
            } else {
                response.sendRedirect("/IS3102_Project-war/B/SG/shoppingCart.jsp"
                    + "?errMsg=Invalid Month.");
            }
            
            if (!request.getParameter("year").equals("") && 
                    isNumeric(request.getParameter("year"))) {
                // Regex crap for year
                // https://stackoverflow.com/questions/44601979/regex-to-check-if-string-contains-year
                if (Pattern.compile("^(19|20)[0-9][0-9]") 
                        .matcher(request.getParameter("year")).matches()) {
                    // If the year is proper, set it
                    year = Integer.parseInt(request.getParameter("year"));
                } else {
                    response.sendRedirect("/IS3102_Project-war/B/SG/shoppingCart.jsp"
                        + "?errMsg=Invalid Year Format (yyyy).");
                }
            } else {
                response.sendRedirect("/IS3102_Project-war/B/SG/shoppingCart.jsp"
                    + "?errMsg=Invalid Year.");
            }
            
            for (ShoppingCartLineItem i : shoppingCart) {
                finalPrice += (i.getPrice() * i.getQuantity()); 
            }
            
            Response paymentRow = createRowAtDB(memberID, finalPrice, countryID);
            
            if (paymentRow.getStatus() == 200) {
                long salesRecordID = Long.parseLong(paymentRow.readEntity(String.class));
                
                // link to sales record
                for (ShoppingCartLineItem item : shoppingCart) {
                    Response res = addItemToRowAtDB(salesRecordID,item);
                    
                    if (res.getStatus() != 200) {
                        response.sendRedirect("/IS3102_Project-war/B/SG/shoppingCart.jsp"
                            + "?errMsg=" + res.readEntity(String.class));
                        return;
                    }
                    
                    long itementityId = Long.parseLong(res.readEntity(String.class));
                    Response lineItemMemberResp = bindItemToMemberAtDB(itementityId, memberID);
                    
                    if (lineItemMemberResp.getStatus() != 200){
                        response.sendRedirect("/IS3102_Project-war/B/SG/shoppingCart.jsp"
                            + "?errMsg=" + lineItemMemberResp.readEntity(String.class));
                        return;
                    }
                }
                
                session.setAttribute("shoppingCart",
                        new ArrayList<>());
                session.setAttribute("transcationId", salesRecordID);
                response.sendRedirect("/IS3102_Project-war/B/SG/shoppingCart.jsp"
                            + "?goodMsg=Transaction complete.");
            } else {
                out.println(paymentRow.readEntity(String.class));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            response.sendRedirect("/IS3102_Project-war/B/SG/shoppingCart.jsp"
                    + "?errMsg=" + ex.getMessage());
        }
    }
    
    public static boolean isNumeric(String s)
        {
          return s.matches("-?\\d+(\\.\\d+)?");
        }

    public Response createRowAtDB(long memberId, double finalPrice,
            long countryId) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client
                .target("http://localhost:8080/IS3102_WebService-Student/webresources/commerce")
                .path("createECommerceTransactionRecord")
                .queryParam("finalPrice", finalPrice)
                .queryParam("countryId", countryId);
        Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON);
        
        return invocationBuilder.put(Entity.entity(String.valueOf(memberId), MediaType.APPLICATION_JSON));
    }
    
    public Response addItemToRowAtDB(long salesRecordId, ShoppingCartLineItem item) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client
                .target("http://localhost:8080/IS3102_WebService-Student/webresources/commerce")
                .path("createECommerceLineItemRecord")
                .queryParam("salesRecordID", salesRecordId)
                .queryParam("itemEntityID", item.getId())
                .queryParam("quantity", item.getQuantity())
                .queryParam("countryID", item.getCountryID());
        Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON);
        
        return invocationBuilder.put(Entity.entity(item, MediaType.APPLICATION_JSON));
    }
    
        public Response bindItemToMemberAtDB(long lineitementityId, 
            long memberId) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client
                .target("http://localhost:8080/IS3102_WebService-Student/webresources/entity.memberentity")
                .path("createECommerceLineItemRecord")
                .queryParam("memberId", memberId);
        
        Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON);
        
        return invocationBuilder.put(Entity.entity(String.valueOf(lineitementityId), MediaType.APPLICATION_JSON));
    }
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
