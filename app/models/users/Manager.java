package models.users;

import java.util.*;
import javax.persistence.*;
import play.data.format.*;
import play.data.validation.*;
import com.avaje.ebean.*;

@Entity
// This is a User of type manager
// Map inherited classes to a single table
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) 
@DiscriminatorValue("manager")

// Manager inherits from the User class
public class Manager extends User {
	
	public String department;
	
	public Manager(String email, String name, String password, String department)
	{
		super(email, name, password);
		this.department = department;
	}
	
    public static Finder<String,Manager> findManager = new Finder<String,Manager>(String.class, Manager.class);


    public static List<Manager> findAll() {
        return Manager.findManager.all();
    }

} 
