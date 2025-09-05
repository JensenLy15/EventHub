package au.edu.rmit.sept.webapp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Event {
  private Long eventId;

  
  private String name;
  private String desc;
  private Long createdByUserId;
  private LocalDateTime dateTime;
  private String location;
  private String category;
  private Integer capacity;
  private Long categoryFkId;
  private BigDecimal price;


  public Event() {
    this.eventId = 0L;
    this.name = "";
    this.desc = "";
    this.createdByUserId = 0L;
    this.dateTime = LocalDateTime.now();
    this.location = "";
    this.category = "";
    this.capacity = 0;
    this.categoryFkId = 0L;
    this.price = BigDecimal.ZERO;
  }


  public Event(Long eventId, String name, String desc, Long createdByUserId, LocalDateTime dateTime, 
                String location, String category, Integer capacity, Long categoryFkId, BigDecimal price) {
                  this.eventId = eventId;
                  this.name = name;
                  this.desc = desc;
                  this.createdByUserId = createdByUserId;
                  this.dateTime = dateTime;
                  this.location = location;
                  this.category = category;
                  this.capacity = capacity;
                  this.categoryFkId = categoryFkId;
                  this.price = price;
                }


  public void setEventId(Long eventId) {
    this.eventId = eventId;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getDesc() {
    return this.desc;
  }
  public void setDesc(String desc) {
    this.desc = desc;
  }
  public void setCreatedByUserId(Long createdByUserId) {
    this.createdByUserId = createdByUserId;
  }
  public void setDateTime(LocalDateTime dateTime) {
    this.dateTime = dateTime;
  }
  public void setLocation(String location) {
    this.location = location;
  }
  public void setCategory(String category) {
    this.category = category;
  }
  public void setCapacity(Integer capacity) {
    this.capacity = capacity;
  }
  public void setCategoryFkId(Long categoryFkId) {
    this.categoryFkId = categoryFkId;
  }
  public void setPrice(BigDecimal price) {
    this.price = price;
  }
  
  public Long getEventId() { return eventId; }
  public String getName() { return name; }
  public String getDescription() { return desc; }
  public Long getCreatedByUserId() { return createdByUserId; }
  public LocalDateTime getDateTime() { return dateTime; }
  public String getLocation() { return location; }
  public String getCategory() { return category; }
  public Integer getCapacity() { return capacity; }
  public Long getCategoryFkId() { return categoryFkId; }
  public java.math.BigDecimal getPrice() { return price; }
}
