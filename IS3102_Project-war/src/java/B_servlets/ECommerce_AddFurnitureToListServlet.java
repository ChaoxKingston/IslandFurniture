/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package B_servlets;

import HelperClasses.Member;
import HelperClasses.ShoppingCartLineItem;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Zamiel Chia
 */
@WebServlet(name = "ECommerce_AddFurnitureToListServlet", urlPatterns = {"/ECommerce_AddFurnitureToListServlet"})
public class ECommerce_AddFurnitureToListServlet extends HttpServlet {

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
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();
        
        String category = (String) session.getAttribute("cat");
        
        try 
        {
            long countryID = 0;
            int quantity = 0;
            
            if (session.getAttribute("countryID") != null) {
                countryID = (long) session.getAttribute("countryID");
            } else {
                response.sendRedirect("/B/selectCountry.jsp");
                return;
            }
           
            if (request.getParameter("SKU").equals("")) {
                response.sendRedirect("/IS3102_Project-war/B/SG/furnitureCategory.jsp"
                    + "&errMsg=Incorrect SKU Code.");
            } 
            
            String sku = (String) request.getParameter("SKU");
            quantity = retrieveQuantity(sku);
            // Check if item is avaliable
            // RESTFul API not working, so cannot check stock, just the base code.
            if(quantity > 0) 
            {
                ArrayList<ShoppingCartLineItem> shoppingCart;
                
                System.out.println("Item is here. Amt: " + quantity);
                // Add item into the cart
                String memberEmail = (String) session.getAttribute("memberEmail");
                
                // Create the object item
                ShoppingCartLineItem cartItem = new ShoppingCartLineItem();
                cartItem.setId(request.getParameter("id"));
                cartItem.setSKU(sku);
                cartItem.setPrice(Double.parseDouble(request.getParameter("price")));
                cartItem.setName(request.getParameter("name"));
                cartItem.setImageURL(request.getParameter("imageURL"));
                cartItem.setCountryID(countryID);
            
                //Check if both of the item and the cart exists
                if(session.getAttribute("shoppingCart") != null)
                {
                    shoppingCart = (ArrayList<ShoppingCartLineItem>) session.getAttribute("shoppingCart");
                    
                    if (shoppingCart.contains(cartItem)) {
                        // Loop the shopping cart !
                        for (int i = 0; i < shoppingCart.size(); i++) {
                            ShoppingCartLineItem currentItem = shoppingCart.get(i);
                            if (currentItem.equals(cartItem)) {
                                if ((currentItem.getQuantity() + 1) <= quantity) {
                                    currentItem.setQuantity(currentItem.getQuantity() + 1);
                                } else {
                                    response.sendRedirect("/IS3102_Project-war/B/SG/furnitureCategory.jsp"
                                        + "&errMsg=There is insufficient stock for your request.");
                                }
                                break;
                            }
                        }
                    } else {
                        cartItem.setQuantity(1);
                        shoppingCart.add(cartItem);
                        out.println("hi i am here 2");
                    }
                } else {
                    // Create the shopping cart
                    shoppingCart = new ArrayList();
                    out.println("hi i am here 1");
                    // Simply add the item to the shopping cart since this is
                    // the first item
                    cartItem.setQuantity(1);
                    shoppingCart.add(cartItem);
                }
                
                session.setAttribute("shoppingCart", shoppingCart);
                response.sendRedirect("/IS3102_Project-war/B/SG/shoppingCart.jsp"
                        +"?goodMsg=" + cartItem.getName() +" has been added into the cart"
                        + " successfully.");
            } else 
            {
                // If there's no stock, tell the user that there's no stock
//                out.println("There's no more stocks. Stocks: " + itemAvaliable(sku));
                response.sendRedirect("/IS3102_Project-war/B/SG/furnitureCategory.jsp"
                + "?errMsg=" + "There's no more stocks.");
            }
        } 
        catch (Exception ex)
        {
            out.println(ex.toString());
            response.sendRedirect("/IS3102_Project-war/B/SG/furnitureCategory.jsp" 
            + "?errMsg=" + ex.toString());
        }
    }

//    // fixed bois actually is storeid wrong xd
//        public boolean itemAvaliable(String sku)
//        {
//            Client client = ClientBuilder.newClient();
//            WebTarget target = client
//                    .target("http://localhost:8080/IS3102_WebService-Student/webresources/entity.storeentity")
//                    .path("getQuantity")
//                    .queryParam("SKU", sku)
//                    .queryParam("storeID", 59);
//            
//            Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON);
//            Response response = invocationBuilder.get();
//            System.out.println("status: " + response.getStatus());
//            
//            String quantityStr = response.readEntity(String.class);
//            int quantity = Integer.parseInt(quantityStr);
//            
//            return quantity > 0;
//        }
        
        public int retrieveQuantity(String sku) {
            Client client = ClientBuilder.newClient();
            WebTarget target = client
                    .target("http://localhost:8080/IS3102_WebService-Student/webresources/entity.storeentity")
                    .path("getQuantity")
                    .queryParam("storeID", 59)
                    .queryParam("SKU", sku);
            Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON);
            Response response = invocationBuilder.get();

            String qtyStr = response.readEntity(String.class);
            int qty = Integer.parseInt(qtyStr);
            //System.out.println("status: " + response.getStatus());
            return qty;
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
