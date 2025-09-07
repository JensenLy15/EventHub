package au.edu.rmit.sept.webapp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Event {
  private Long eventId;
  private String name;
  private String desc;
  private Long createdByUserId;
  private LocalDateTime dateTime;
  private String location;
  private Integer capacity;
  private BigDecimal price;
  private List<String> categories;

  public Event() {
    this.eventId = 0L;
    this.name = "";
    this.desc = "";
    this.createdByUserId = 0L;
    this.dateTime = LocalDateTime.now();
    this.location = "";
    this.capacity = 0;
    this.categories = List.of();
    this.price = BigDecimal.ZERO;
  }


  public Event(Long eventId, String name, String desc, Long createdByUserId, LocalDateTime dateTime, 
                String location, List<String> categories, Integer capacity, BigDecimal price) {
                  this.eventId = eventId;
                  this.name = name;
                  this.desc = desc;
                  this.createdByUserId = createdByUserId;
                  this.dateTime = dateTime;
                  this.location = location;
                  this.categories = categories;
                  this.capacity = capacity;
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
  public void setCategory(List<String> category) {
    this.categories = category;
  }
  public void setCapacity(Integer capacity) {
    this.capacity = capacity;
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
  public List<String> getCategory() { return categories; }
  public Integer getCapacity() { return capacity; }
  public java.math.BigDecimal getPrice() { return price; }
}
