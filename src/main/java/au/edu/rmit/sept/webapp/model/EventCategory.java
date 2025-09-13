package au.edu.rmit.sept.webapp.model;

public class EventCategory {
    private Long categoryId;
    private String name;

    public EventCategory(Long categoryId, String name){
        this.categoryId = categoryId;
        this.name = name;
    }

    public Long getCategoryId(){return categoryId;}
    public void setCategoryId(Long categoryId) {this.categoryId = categoryId;}
    
    public String getName(){return name;}
    public void setName(String name){this.name = name;}
}
