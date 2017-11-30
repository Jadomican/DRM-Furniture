package models.shopping;

import java.util.*;
import javax.persistence.*;

import play.data.format.*;
import play.data.validation.*;
// OrderItem entity managed by Ebean
import com.avaje.ebean.*;

import models.products.*;
import models.users.*;

@Entity
public class OrderItem extends Model {

    @Id
    public Long id;

    @ManyToOne
    public ShopOrder order;
    
    @ManyToOne
    public Basket basket;
    
    // Unidirection mapping - Many order items can have one product
    // Product not interested in this
    @ManyToOne
    public Product product;
    
    public int quantity;
    public double price;

    // Default constructor
    public  OrderItem() {
    }
    
    public OrderItem(Product p) {
            product = p;
            quantity = 1;
            price = p.price;
    }
    
    // Increment quantity
    public void increaseQty() {
        quantity += 1;
    }
    
    // Decrement quantity
    public void decreaseQty() {
        quantity -= 1;
    }
    
    // Calculate and return total price for this order item
    public double getItemTotal() {
        return this.price * this.quantity;
    }

		public int getQuantity(){
				return quantity;
		}
	
		//Generic query helper
    public static Finder<Long,OrderItem> find = new Finder<Long,OrderItem>(OrderItem.class);

    //Find all Products in the database
    public static List<OrderItem> findAll() {
        return OrderItem.find.all();
    }
	
}

