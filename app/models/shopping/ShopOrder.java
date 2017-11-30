package models.shopping;

import java.util.*;
import javax.persistence.*;
import java.util.Date;

import play.data.format.*;
import play.data.validation.*;
// ShopOrder entity managed by Ebean
import com.avaje.ebean.*;

import models.products.*;
import models.users.*;

@Entity
public class ShopOrder extends Model {

    @Id
    public Long id;
    
    public Date OrderDate;
    
    // Order contains may items.
    // mappedBy makes this side of the mapping the owner
    // so foreign key will be placed in resulting OrderItem table
    // All changes, including deletes will be cascaded
    @OneToMany(mappedBy="order", cascade = CascadeType.ALL)
    public List<OrderItem> items;
    
    @ManyToOne
    public Customer customer;

    // Default constructor
    public  ShopOrder() {
        OrderDate = new Date();
    }

    public Date getDate() {
			return OrderDate;
    }
    
    public double getOrderTotal() {
        
        double total = 0;
        
        for (OrderItem i: items) {
            total += i.getItemTotal();
        }
        return total;
    }

    public double getOrderVat() {
        double total = 0;
        for (OrderItem i: items) {
            total += i.getItemTotal();
        }
        return total * 0.21;
    }

    public double getOrderVatTotal() {
        double total = 0;
        for (OrderItem i: items) {
            total += i.getItemTotal();
        }
        return total + total * 0.21;
    }
	
		//Generic query helper
    public static Finder<Long,ShopOrder> find = new Finder<Long,ShopOrder>(ShopOrder.class);
		public static Finder<String,ShopOrder> findString = new Finder<String,ShopOrder>(ShopOrder.class);

    //Find all Products in the database
    public static List<ShopOrder> findAll() {
        return ShopOrder.find.all();
    }
	
    public static List<ShopOrder> findByEmail() {
        return ShopOrder.find.all();
    }

}

