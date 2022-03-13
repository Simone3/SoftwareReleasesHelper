package com.utils.model.properties;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.utils.model.git.GitData;
import com.utils.model.jenkins.JenkinsData;
import com.utils.model.main.Category;

import lombok.Data;

/**
 * Helper POJO that contains the parsed application properties
 */
@Data
@ConfigurationProperties(ignoreInvalidFields = true, ignoreUnknownFields = true)
public class Properties {
	
	private boolean testMode;
	private boolean printPasswords;
	private GitData git;
	private JenkinsData jenkins;
	private List<Category> categories;
	private String optionalPreSelectedCategoryIndex;
	private String optionalPreSelectedProjectIndices;
}
