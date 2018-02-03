/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package B_servlets;

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

/**
 *
 * @author Zamiel Chia
 */
@WebServlet(name = "ECommerce_MinusFurnitureToListServlet", urlPatterns = {"/ECommerce_MinusFurnitureToListServlet"})
public class ECommerce_MinusFurnitureToListServlet extends HttpServlet {

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
            if(!"".equals(request.getParameter("SKU"))){
                String sku = request.getParameter("SKU");
                int quantity = 0;
                ShoppingCartLineItem cartItem = new ShoppingCartLineItem();
                
                // Go through the array list
                ArrayList<ShoppingCartLineItem> shoppingCart;
                // Check if shopping cart exists
                if (session.getAttribute("shoppingCart") != null){
                    shoppingCart =(ArrayList<ShoppingCartLineItem>)
                            session.getAttribute("shoppingCart");
                    
                    for (ShoppingCartLineItem x : shoppingCart) {
                        if(x.getSKU().equals(sku)) {
                            if(x.getQuantity() == 1) {
                                cartItem.setName(x.getName());
                                shoppingCart.remove(x);
                                break;
                            }
                            quantity = x.getQuantity() - 1;
                            x.setQuantity(quantity);
                            cartItem = x;
                            break;
                        }
                    }
                    session.setAttribute("shoppingCart", shoppingCart);
                    
                    if(quantity != 0){
                        response.sendRedirect("/IS3102_Project-war/B/SG/shoppingCart.jsp"
                             + "?goodMsg=1 " + cartItem.getName() + " successfully removed from your cart.");
                    } else {
                        response.sendRedirect("/IS3102_Project-war/B/SG/shoppingCart.jsp"
                             + "?goodMsg=" + cartItem.getName() + " successfully removed from your cart.");
                    }
                }
            } else {
                response.sendRedirect("/IS3102_Project-war/B/SG/shoppingCart.jsp"
                    + "?errMsg=Invalid SKU.");            
            }
        } catch (Exception ex) {
                response.sendRedirect("/IS3102_Project-war/B/SG/shoppingCart.jsp"
                    + "?errMsg=" + ex.toString());
        }
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
