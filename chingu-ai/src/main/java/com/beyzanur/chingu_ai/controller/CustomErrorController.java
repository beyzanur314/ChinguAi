package com.beyzanur.chingu_ai.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        System.out.println("❌ ERROR STATUS: " + status);
        System.out.println("❌ ERROR MESSAGE: " + message);

        if (exception != null) {
            ((Exception) exception).printStackTrace();
            model.addAttribute("error", exception.toString());
        }

        model.addAttribute("status", status);
        model.addAttribute("message", message);

        return "error";
    }
}