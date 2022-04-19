package com.utils.releaseshelper.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.utils.releaseshelper.model.logic.Category;
import com.utils.releaseshelper.model.logic.Project;
import com.utils.releaseshelper.model.logic.action.Action;
import com.utils.releaseshelper.model.properties.CategoryProperty;
import com.utils.releaseshelper.validation.ValidationException;
import com.utils.releaseshelper.validation.ValidationUtils;

import lombok.experimental.UtilityClass;

/**
 * Maps and validates category properties
 */
@UtilityClass
public class CategoryMapperValidator {

	public static List<Category> mapAndValidateCategories(List<CategoryProperty> categoriesProperties, Map<String, Action> actionDefinitions) {
		
		ValidationUtils.notEmpty(categoriesProperties, "At least one category should be defined");
		
		List<Category> categories = new ArrayList<>();
		Map<String, Void> categoryNames = new HashMap<>();
		
		for(int i = 0; i < categoriesProperties.size(); i++) {

			CategoryProperty categoryProperty = categoriesProperties.get(i);
			
			Category category;
			try {
				
				category = mapAndValidateCategory(categoryProperty, actionDefinitions);
			}
			catch(Exception e) {
				
				throw new ValidationException("Invalid category at index " + i + " -> " + e.getMessage());
			}

			String categoryName = categoryProperty.getName();
			if(categoryNames.containsKey(categoryName)) {

				throw new ValidationException("Category at index " + i + " has the same name of a previous category");
			}
			
			categoryNames.put(categoryName, null);
			categories.add(category);
		}
		
		return categories;
	}
	
	public static Category mapAndValidateCategory(CategoryProperty categoryProperty, Map<String, Action> actionDefinitions) {
		
		ValidationUtils.notNull(categoryProperty, "Category is empty");
		
		String name = ValidationUtils.notBlank(categoryProperty.getName(), "Category does not have a name");
		List<Project> projects = ProjectMapperValidator.mapAndValidateProjects(categoryProperty.getProjects(), actionDefinitions);
		
		Category category = new Category();
		category.setName(name);
		category.setProjects(projects);
		return category;
	}
}
