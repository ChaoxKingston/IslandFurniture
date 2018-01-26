/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package B_servlets;


import HelperClasses.Member;
import java.io.IOException;
import java.io.PrintWriter;
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
@WebServlet(name = "ECommerce_MemberEditProfileServlet", urlPatterns = {"/ECommerce_MemberEditProfileServlet"})
public class ECommerce_MemberEditProfileServlet extends HttpServlet {
    
    private String result;

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
        Member member = new Member();
        HttpSession session = request.getSession();
        
        try 
        {
        String email = (String) session.getAttribute("memberEmail");
        member.setEmail(email);
        
        /* server-side verification, so no funny business */
        String name = request.getParameter("name");
        if (name.equals("") || (name.equals(null)))
        {
            result = "Please enter your name.";
            response.sendRedirect("/IS3102_Project-war/B/SG/memberProfile.jsp?errMsg=" + result);   
        }
        member.setName(name);
        // Phone
        String phone = request.getParameter("phone");
        if (phone.equals("") || phone.equals(null)) 
        {
            result = "Please enter your phone number.";
            response.sendRedirect("/IS3102_Project-war/B/SG/memberProfile.jsp?errMsg=" + result);                
        }
        member.setPhone(phone);
        // address          
        String address = request.getParameter("address");
        if (address.equals("") || address.equals(null)) 
        {
            result = "Please enter your address.";
            response.sendRedirect("/IS3102_Project-war/B/SG/memberProfile.jsp?errMsg=" + result);                
        }
        member.setAddress(address);
        // securityQuestion    
        int securityQuestion = Integer.parseInt(request.getParameter("securityQuestion"));
        if (securityQuestion < 1 || securityQuestion > 3) 
        {
            result = "Please select a security question.";
            response.sendRedirect("/IS3102_Project-war/B/SG/memberProfile.jsp?errMsg=" + result);                
        }
        member.setSecurityQuestion(securityQuestion);
        // securityAnswer    
        String securityAnswer = request.getParameter("securityAnswer");
        if (securityAnswer.equals("") || securityAnswer.equals(null)) 
        {
            result = "Please enter your security answer.";
            response.sendRedirect("/IS3102_Project-war/B/SG/memberProfile.jsp?errMsg=" + result);                
        }
        member.setSecurityAnswer(securityAnswer);
        // Age
        int age = Integer.parseInt(request.getParameter("age"));
        if (age < 0 || age > 150) 
        {
            result = "Please enter a valid age.";
            response.sendRedirect("/IS3102_Project-war/B/SG/memberProfile.jsp?errMsg=" + result);                
        }
        member.setAge(age);
        // Income
        int income = Integer.parseInt(request.getParameter("income"));
        if (income < 0) {
            result = "Please enter a valid income.";
            response.sendRedirect("/IS3102_Project-war/B/SG/memberProfile.jsp?errMsg=" + result);                
        }
        member.setIncome(income);
        // Password
        String password = request.getParameter("password");      
        if (password != null && password != "") 
        {
            String repassword = request.getParameter("repassword");
            if (repassword.equals(password)) { 
                // If the repeated password is correct
                result = updateMemberDetails(member, password);
                response.sendRedirect("/IS3102_Project-war/B/SG/memberProfile.jsp?" + result);
            } else {
                // Make sure that user entered passwword correctly
                result = "errMsg=Your password does not match the repeated input.";
                response.sendRedirect("/IS3102_Project-war/B/SG/memberProfile.jsp?" + result);  
            }
        } 
        else 
        { // Only update details
            result = updateMemberDetails(member, null);
            response.sendRedirect("/IS3102_Project-war/B/SG/memberProfile.jsp?" + result);  
        }            
        } catch (Exception e) {
            e.printStackTrace();
            out.println(e.toString());
        }
 }
    
    public String updateMemberDetails(Member member, String password) {
        try 
        {
            Client client = ClientBuilder.newClient();
            WebTarget target = client
                    .target("http://localhost:8080/IS3102_WebService-Student/webresources/entity.memberentity").path("updatemember")
                    .queryParam("password", password);
            
            Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON);
            Response response = invocationBuilder.put(Entity.entity(member, MediaType.APPLICATION_JSON));
            System.out.println("status: " + response.getStatus());
            
            if (response.getStatus() == 200) {
                return "goodMsg=Your profile has been updated.";
            }
            
            return "errMsg=IT WONT WORK U NOOB" + response.getStatus();
        } catch (Exception ex) {
            ex.printStackTrace();
            return ex.getMessage();
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
