package com.utils.releaseshelper.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.thymeleaf.spring5.context.webflux.IReactiveDataDriverContextVariable;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;

@Controller
public class GuiController {

	@Autowired
    private ReactiveMovieRepository movieRepository;
    
	@GetMapping("/")
	public String main(Model model) {
		
		
//		model.addAttribute("myData", new Random().nextInt());
		
		IReactiveDataDriverContextVariable reactiveDataDrivenMode =
				new ReactiveDataDriverContextVariable(movieRepository.findAll(), 1);
		
		model.addAttribute("movies", reactiveDataDrivenMode);
		
		return "index";
	}
}
