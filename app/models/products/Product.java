package models.products;

import java.util.*;
import javax.persistence.*;

import play.data.format.*;
import play.data.validation.*;
// Product entity managed by Ebean
import com.avaje.ebean.*;

import models.shopping.*;

@Entity
public class Product extends Model {

    // Fields defined as public and not private
    // During compile time, The Play Framework
    // automatically generates getters and setters
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Long id;

    // many to many mapping
    @ManyToMany(cascade = CascadeType.ALL, mappedBy = "products")
    public List<Category> categories;
    
    // List of category ids - this will be bound to checkboxes in the view form
    public List<Long> catSelect = new ArrayList<Long>();

    @Constraints.Required
    public String name;

    @Constraints.Required
    public String description;

    @Constraints.Required
    public int	stock;

    @Constraints.Required
    public double price;

    // Default constructor
    public  Product() {
    }

    // Constructor to initialise object
    public  Product(Long id, String name, String description, int stock, double price){
        this.id = id;
        this.name = name;
        this.description = description;
        this.stock = stock;
        this.price = price;
    }
	
	//Generic query helper for entity Computer with id Long
    public static Finder<Long,Product> find = new Finder<Long,Product>(Long.class, Product.class);

    //Find all Products in the database
    public static List<Product> findAll() {
        return Product.find.all();
    }

		public void increaseStock(int quantity){
			stock += quantity;
		}

		public void decreaseStock(int quantity){
			stock -= quantity;
		}
	
}

