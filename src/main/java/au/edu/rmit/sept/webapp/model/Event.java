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
