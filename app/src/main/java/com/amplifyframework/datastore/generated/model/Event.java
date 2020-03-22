package com.amplifyframework.datastore.generated.model;


import androidx.core.util.ObjectsCompat;

import com.amplifyframework.core.model.Model;
import com.amplifyframework.core.model.annotations.Index;
import com.amplifyframework.core.model.annotations.ModelConfig;
import com.amplifyframework.core.model.annotations.ModelField;
import com.amplifyframework.core.model.query.predicate.QueryField;

import java.util.Objects;
import java.util.UUID;

import static com.amplifyframework.core.model.query.predicate.QueryField.field;

/** This is an auto generated class representing the Event type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "Events")
@Index(name = "UserEvents", fields = {"userSub","timestamp"})
public final class Event implements Model {
  public static final QueryField ID = field("id");
  public static final QueryField ACTIVITY = field("activity");
  public static final QueryField USER_SUB = field("userSub");
  public static final QueryField TIMESTAMP = field("timestamp");
  public static final QueryField AT_HOME = field("atHome");
  public static final QueryField LAT = field("lat");
  public static final QueryField LNG = field("lng");
  private final @ModelField(targetType="ID", isRequired = true) String id;
  private final @ModelField(targetType="String") String activity;
  private final @ModelField(targetType="String", isRequired = true) String userSub;
  private final @ModelField(targetType="String", isRequired = true) String timestamp;
  private final @ModelField(targetType="Location") Location atHome;
  private final @ModelField(targetType="String") String lat;
  private final @ModelField(targetType="String") String lng;
  public String getId() {
      return id;
  }
  
  public String getActivity() {
      return activity;
  }
  
  public String getUserSub() {
      return userSub;
  }
  
  public String getTimestamp() {
      return timestamp;
  }
  
  public Location getAtHome() {
      return atHome;
  }
  
  public String getLat() {
      return lat;
  }
  
  public String getLng() {
      return lng;
  }
  
  private Event(String id, String activity, String userSub, String timestamp, Location atHome, String lat, String lng) {
    this.id = id;
    this.activity = activity;
    this.userSub = userSub;
    this.timestamp = timestamp;
    this.atHome = atHome;
    this.lat = lat;
    this.lng = lng;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      Event event = (Event) obj;
      return ObjectsCompat.equals(getId(), event.getId()) &&
              ObjectsCompat.equals(getActivity(), event.getActivity()) &&
              ObjectsCompat.equals(getUserSub(), event.getUserSub()) &&
              ObjectsCompat.equals(getTimestamp(), event.getTimestamp()) &&
              ObjectsCompat.equals(getAtHome(), event.getAtHome()) &&
              ObjectsCompat.equals(getLat(), event.getLat()) &&
              ObjectsCompat.equals(getLng(), event.getLng());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getActivity())
      .append(getUserSub())
      .append(getTimestamp())
      .append(getAtHome())
      .append(getLat())
      .append(getLng())
      .toString()
      .hashCode();
  }
  
  public static UserSubStep builder() {
      return new Builder();
  }
  
  /** 
   * WARNING: This method should not be used to build an instance of this object for a CREATE mutation.
   * This is a convenience method to return an instance of the object with only its ID populated
   * to be used in the context of a parameter in a delete mutation or referencing a foreign key
   * in a relationship.
   * @param id the id of the existing item this instance will represent
   * @return an instance of this model with only ID populated
   * @throws IllegalArgumentException Checks that ID is in the proper format
   */
  public static Event justId(String id) {
    try {
      UUID.fromString(id); // Check that ID is in the UUID format - if not an exception is thrown
    } catch (Exception exception) {
      throw new IllegalArgumentException(
              "Model IDs must be unique in the format of UUID. This method is for creating instances " +
              "of an existing object with only its ID field for sending as a mutation parameter. When " +
              "creating a new object, use the standard builder method and leave the ID field blank."
      );
    }
    return new Event(
      id,
      null,
      null,
      null,
      null,
      null,
      null
    );
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(id,
      activity,
      userSub,
      timestamp,
      atHome,
      lat,
      lng);
  }
  public interface UserSubStep {
    TimestampStep userSub(String userSub);
  }
  

  public interface TimestampStep {
    BuildStep timestamp(String timestamp);
  }
  

  public interface BuildStep {
    Event build();
    BuildStep id(String id) throws IllegalArgumentException;
    BuildStep activity(String activity);
    BuildStep atHome(Location atHome);
    BuildStep lat(String lat);
    BuildStep lng(String lng);
  }
  

  public static class Builder implements UserSubStep, TimestampStep, BuildStep {
    private String id;
    private String userSub;
    private String timestamp;
    private String activity;
    private Location atHome;
    private String lat;
    private String lng;
    @Override
     public Event build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new Event(
          id,
          activity,
          userSub,
          timestamp,
          atHome,
          lat,
          lng);
    }
    
    @Override
     public TimestampStep userSub(String userSub) {
        Objects.requireNonNull(userSub);
        this.userSub = userSub;
        return this;
    }
    
    @Override
     public BuildStep timestamp(String timestamp) {
        Objects.requireNonNull(timestamp);
        this.timestamp = timestamp;
        return this;
    }
    
    @Override
     public BuildStep activity(String activity) {
        this.activity = activity;
        return this;
    }
    
    @Override
     public BuildStep atHome(Location atHome) {
        this.atHome = atHome;
        return this;
    }
    
    @Override
     public BuildStep lat(String lat) {
        this.lat = lat;
        return this;
    }
    
    @Override
     public BuildStep lng(String lng) {
        this.lng = lng;
        return this;
    }
    
    /** 
     * WARNING: Do not set ID when creating a new object. Leave this blank and one will be auto generated for you.
     * This should only be set when referring to an already existing object.
     * @param id id
     * @return Current Builder instance, for fluent method chaining
     * @throws IllegalArgumentException Checks that ID is in the proper format
     */
    public BuildStep id(String id) throws IllegalArgumentException {
        this.id = id;
        
        try {
            UUID.fromString(id); // Check that ID is in the UUID format - if not an exception is thrown
        } catch (Exception exception) {
          throw new IllegalArgumentException("Model IDs must be unique in the format of UUID.",
                    exception);
        }
        
        return this;
    }
  }
  

  public final class CopyOfBuilder extends Builder {
    private CopyOfBuilder(String id, String activity, String userSub, String timestamp, Location atHome, String lat, String lng) {
      super.id(id);
      super.userSub(userSub)
        .timestamp(timestamp)
        .activity(activity)
        .atHome(atHome)
        .lat(lat)
        .lng(lng);
    }
    
    @Override
     public CopyOfBuilder userSub(String userSub) {
      return (CopyOfBuilder) super.userSub(userSub);
    }
    
    @Override
     public CopyOfBuilder timestamp(String timestamp) {
      return (CopyOfBuilder) super.timestamp(timestamp);
    }
    
    @Override
     public CopyOfBuilder activity(String activity) {
      return (CopyOfBuilder) super.activity(activity);
    }
    
    @Override
     public CopyOfBuilder atHome(Location atHome) {
      return (CopyOfBuilder) super.atHome(atHome);
    }
    
    @Override
     public CopyOfBuilder lat(String lat) {
      return (CopyOfBuilder) super.lat(lat);
    }
    
    @Override
     public CopyOfBuilder lng(String lng) {
      return (CopyOfBuilder) super.lng(lng);
    }
  }
  
}
