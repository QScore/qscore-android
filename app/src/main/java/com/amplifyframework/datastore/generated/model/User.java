package com.amplifyframework.datastore.generated.model;


import androidx.core.util.ObjectsCompat;

import com.amplifyframework.core.model.Model;
import com.amplifyframework.core.model.annotations.ModelConfig;
import com.amplifyframework.core.model.annotations.ModelField;
import com.amplifyframework.core.model.query.predicate.QueryField;

import java.util.Objects;
import java.util.UUID;

import static com.amplifyframework.core.model.query.predicate.QueryField.field;

/** This is an auto generated class representing the User type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "Users")
public final class User implements Model {
  public static final QueryField ID = field("id");
  public static final QueryField SUB = field("sub");
  public static final QueryField AVATAR = field("avatar");
  private final @ModelField(targetType="ID", isRequired = true) String id;
  private final @ModelField(targetType="ID", isRequired = true) String sub;
  private final @ModelField(targetType="String") String avatar;
  public String getId() {
      return id;
  }
  
  public String getSub() {
      return sub;
  }
  
  public String getAvatar() {
      return avatar;
  }
  
  private User(String id, String sub, String avatar) {
    this.id = id;
    this.sub = sub;
    this.avatar = avatar;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      User user = (User) obj;
      return ObjectsCompat.equals(getId(), user.getId()) &&
              ObjectsCompat.equals(getSub(), user.getSub()) &&
              ObjectsCompat.equals(getAvatar(), user.getAvatar());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getSub())
      .append(getAvatar())
      .toString()
      .hashCode();
  }
  
  public static SubStep builder() {
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
  public static User justId(String id) {
    try {
      UUID.fromString(id); // Check that ID is in the UUID format - if not an exception is thrown
    } catch (Exception exception) {
      throw new IllegalArgumentException(
              "Model IDs must be unique in the format of UUID. This method is for creating instances " +
              "of an existing object with only its ID field for sending as a mutation parameter. When " +
              "creating a new object, use the standard builder method and leave the ID field blank."
      );
    }
    return new User(
      id,
      null,
      null
    );
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(id,
      sub,
      avatar);
  }
  public interface SubStep {
    BuildStep sub(String sub);
  }
  

  public interface BuildStep {
    User build();
    BuildStep id(String id) throws IllegalArgumentException;
    BuildStep avatar(String avatar);
  }
  

  public static class Builder implements SubStep, BuildStep {
    private String id;
    private String sub;
    private String avatar;
    @Override
     public User build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new User(
          id,
          sub,
          avatar);
    }
    
    @Override
     public BuildStep sub(String sub) {
        Objects.requireNonNull(sub);
        this.sub = sub;
        return this;
    }
    
    @Override
     public BuildStep avatar(String avatar) {
        this.avatar = avatar;
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
    private CopyOfBuilder(String id, String sub, String avatar) {
      super.id(id);
      super.sub(sub)
        .avatar(avatar);
    }
    
    @Override
     public CopyOfBuilder sub(String sub) {
      return (CopyOfBuilder) super.sub(sub);
    }
    
    @Override
     public CopyOfBuilder avatar(String avatar) {
      return (CopyOfBuilder) super.avatar(avatar);
    }
  }
  
}
