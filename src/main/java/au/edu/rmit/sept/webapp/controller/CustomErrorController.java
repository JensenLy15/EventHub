package au.edu.rmit.sept.webapp.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        
        String statusCode = "Unknown";
        String title = "An Error Occurred";
        String message = "Something went wrong";

        // Map specific status codes to custom messages
        //the reason why we do this is beceause we have role base access control
        //so if a user try to access a page that they dont have permission to access
        // and it make the ui look better then the defaul and have option to go back homm

        if (status != null) {
            int code = Integer.parseInt(status.toString());
            statusCode = String.valueOf(code);

            switch (code) {
                case 404 -> {
                    title = "Page not found";
                    message = "The page you're looking for doesn't exist";
                }
                case 403 -> {
                    title = "Access Denied";
                    message = "You don't have permission to access this resource";
                }
                case 500 -> {
                    title = "Internal Server Error";
                    message = "Something went wrong on our end";
                }
                case 400 -> {
                    title = "Bad Request";
                    message = "The request could not be understood";
                }
            }
        }

        model.addAttribute("statusCode", statusCode);
        model.addAttribute("title", title);
        model.addAttribute("message", message);

        return "error";
    }
}