package au.edu.rmit.sept.webapp.service;

import java.util.List;

import org.springframework.stereotype.Service;

import au.edu.rmit.sept.webapp.model.EventCategory;
import au.edu.rmit.sept.webapp.repository.CategoryRepository;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<EventCategory> getAllCategories() {
        return categoryRepository.findAll();
    }

    public void deleteCategories(Long categoryId)
    {
        categoryRepository.deleteCategoryById(categoryId);
    }

    public List<String> findCategoryNamesByIds(List<Long> categoryIds)
    {
        return categoryRepository.findNamesByIds(categoryIds);
    }


}
