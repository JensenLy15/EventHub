package au.edu.rmit.sept.webapp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class Event {
  private Long eventId;

  @NotBlank(message = "Name is required")
  @Size(min = 2, max = 30)
  private String name;

  @NotBlank(message = "Description is required")
  @Size(min = 2, max = 75)
  private String desc;

  private Long createdByUserId;

  @Future(message = "Date must be in the future")
  private LocalDateTime dateTime;

  @NotBlank(message = "Location is required")
  @Size(min = 2, max = 40)
  private String location;

  @NotNull(message = "capacity is required")
  @Min(value = 0, message = "Capacity must be non-negative")
  @Max(value = 1000000, message = "Capacity must not exceed 1000000")
  private Integer capacity;

  @NotNull(message = "Price is required")
  @DecimalMin(value = "0.0", inclusive = true, message = "Price must be non-negative")
  private BigDecimal price;
  
  private String imageUrl;
  
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
    this.imageUrl = "/meetup.jpg";
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
                  this.imageUrl = "/meetup.jpg";
                }

  public void setImageUrl(String imageUrl){
    this.imageUrl = imageUrl;
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
  
  public String getImageUrl() { return imageUrl; } 
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
