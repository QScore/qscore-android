package com.amplifyframework.datastore.generated.model;

import com.amplifyframework.core.model.annotations.BelongsTo;

import java.util.List;
import java.util.UUID;
import java.util.Objects;

import androidx.core.util.ObjectsCompat;

import com.amplifyframework.core.model.Model;
import com.amplifyframework.core.model.annotations.Index;
import com.amplifyframework.core.model.annotations.ModelConfig;
import com.amplifyframework.core.model.annotations.ModelField;
import com.amplifyframework.core.model.query.predicate.QueryField;

import static com.amplifyframework.core.model.query.predicate.QueryField.field;

/** This is an auto generated class representing the Event type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "Events")
public final class Event implements Model {
  public static final QueryField ID = field("id");
  public static final QueryField TYPE = field("type");
  public static final QueryField USER = field("eventUserId");
  private final @ModelField(targetType="ID", isRequired = true) String id;
  private final @ModelField(targetType="String", isRequired = true) String type;
  private final @ModelField(targetType="User") @BelongsTo(targetName = "eventUserId", type = User.class) User user;
  public String getId() {
      return id;
  }
  
  public String getType() {
      return type;
  }
  
  public User getUser() {
      return user;
  }
  
  private Event(String id, String type, User user) {
    this.id = id;
    this.type = type;
    this.user = user;
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
              ObjectsCompat.equals(getType(), event.getType()) &&
              ObjectsCompat.equals(getUser(), event.getUser());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getType())
      .append(getUser())
      .toString()
      .hashCode();
  }
  
  public static TypeStep builder() {
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
      null
    );
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(id,
      type,
      user);
  }
  public interface TypeStep {
    BuildStep type(String type);
  }
  

  public interface BuildStep {
    Event build();
    BuildStep id(String id) throws IllegalArgumentException;
    BuildStep user(User user);
  }
  

  public static class Builder implements TypeStep, BuildStep {
    private String id;
    private String type;
    private User user;
    @Override
     public Event build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new Event(
          id,
          type,
          user);
    }
    
    @Override
     public BuildStep type(String type) {
        Objects.requireNonNull(type);
        this.type = type;
        return this;
    }
    
    @Override
     public BuildStep user(User user) {
        this.user = user;
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
    private CopyOfBuilder(String id, String type, User user) {
      super.id(id);
      super.type(type)
        .user(user);
    }
    
    @Override
     public CopyOfBuilder type(String type) {
      return (CopyOfBuilder) super.type(type);
    }
    
    @Override
     public CopyOfBuilder user(User user) {
      return (CopyOfBuilder) super.user(user);
    }
  }
  
}
