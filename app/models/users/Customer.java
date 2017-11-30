package models.users;

import java.util.*;
import javax.persistence.*;
import play.data.format.*;
import play.data.validation.*;
import com.avaje.ebean.*;

import models.shopping.*;
import models.products.*;

@Entity
// Map inherited classes to a single table
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) 
// This is a Customer of type admin
@DiscriminatorValue("customer")

// Customer inherits from the User class
public class Customer extends User{
	
	public String street1;
	public String street2;
    public String town;
    public String postCode;
    public String creditCard;
    
    // Customer has one basket.
    // Customer is the owner (forieng key will be added to Basket table)
    // All changes to Customer are cascaded.
    @OneToOne(mappedBy="customer", cascade = CascadeType.ALL)
    public Basket basket;

    // Customer can have many ShopOrders.
    // Customer is the owner (foreign key will be added to Basket table)
    // All changes to Customer are cascaded
    @OneToMany(mappedBy="customer", cascade = CascadeType.ALL)
    public List<ShopOrder> orders;
	
	public Customer(String email, String name, String password, String street1, String street2, String town, String postCode, String creditCard)
	{
		super(email, name, password);
        this.street1 = street1;
        this.street2 = street2;
        this.town = town;
        this.postCode = postCode;
		this.creditCard = creditCard;
	}

    public static List<Customer> findAll() {
        return Customer.find.all();
    }

    public Boolean hasOrder() {
			boolean result = false;
			if(orders.size() > 0){
				result = true;
			}
			return result;
				
    }
    
	//Generic query helper for entity User with unique id String
    public static Finder<String,Customer> find = new Finder<String,Customer>(Customer.class);
    
        // Check if a user is logged in (by id - email address)
    public static Customer getLoggedIn(String id) {
        if (id == null)
                return null;
        else
            // Find user by id and return object
            return find.byId(id);
    }
} 
