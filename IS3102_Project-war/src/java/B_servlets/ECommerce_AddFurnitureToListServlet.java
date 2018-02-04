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
        long countryID = (long) session.getAttribute("countryID");
        
        try 
        {
            String sku = (String) request.getParameter("SKU");
            // Check if item is avaliable
            // RESTFul API not working, so cannot check stock, just the base code.
            if(itemAvaliable(sku)) 
            {
                ArrayList<ShoppingCartLineItem> shoppingCart;
                
                System.out.println("Item is here");
                // Add item into the cart
                String memberEmail = (String) session.getAttribute("memberEmail");
                
                // Create the object item
                ShoppingCartLineItem cartItem = new ShoppingCartLineItem();
                cartItem.setId(request.getParameter("id"));
                cartItem.setSKU(sku);
                cartItem.setName(request.getParameter("name"));
                cartItem.setPrice(Double.parseDouble(request.getParameter("price")));
                cartItem.setImageURL(request.getParameter("imageURL"));
                cartItem.setCountryID(countryID);
            
                //Check if both of the item and the cart exists
                if(session.getAttribute("shoppingcart") != null)
                {
                    shoppingCart = (ArrayList<ShoppingCartLineItem>) session.getAttribute("shoppingCart");
                    
                    if (shoppingCart.contains(cartItem)) {
                        for (int x = 0; x < shoppingCart.size(); x++) {
                            ShoppingCartLineItem currentItem = shoppingCart.get(x);
                            if (currentItem.equals(cartItem)) {
                                currentItem.setQuantity(currentItem.getQuantity() + 1);
                                break;
                            }
                        }
                    } else {
                        cartItem.setQuantity(1);
                        shoppingCart.add(cartItem);
                    }
                    
                } else 
                {
                    //Create shopping cart
                    shoppingCart = new ArrayList();
                    //Add the object into the shopping cart
                    cartItem.setQuantity(1);
                    shoppingCart.add(cartItem);
                }
                
                session.setAttribute("shoppingCart", shoppingCart);
                response.sendRedirect("/IS3102_Project-war/B/SG/shoppingCart.jsp"
                        +"?goodMsg=" + cartItem.getName() +"has been added into the cart"
                        + "successfully.");
            } else 
            {
                // If there's no stock, tell the user that there's no stock
                out.println("There's no more stocks. Stocks: " + itemAvaliable(sku));
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

    // fixed bois actually is storeid wrong xd
        public boolean itemAvaliable(String sku)
        {
            Client client = ClientBuilder.newClient();
            WebTarget target = client
                    .target("http://localhost:8080/IS3102_WebService-Student/webresources/entity.storeentity")
                    .path("getQuantity")
                    .queryParam("SKU", sku)
                    .queryParam("storeID", 59);
            
            Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON);
            Response response = invocationBuilder.get();
            System.out.println("status: " + response.getStatus());
            
            String quantityStr = response.readEntity(String.class);
            int quantity = Integer.parseInt(quantityStr);
            
            return quantity > 0;
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
