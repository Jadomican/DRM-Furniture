package controllers;

import play.*;
import play.mvc.*;
import play.data.*;
import play.data.Form.*;
import play.mvc.Http.Context;

import java.util.ArrayList;
import java.util.List;

import java.io.*;

import views.html.*;

// Import models
import models.*;
import models.users.*;
import models.products.*;
import models.shopping.*;

// Import security controllers
import controllers.security.*;

// Authenticate user
@Security.Authenticated(Secured.class)
// Authorise user (check if user is a customer)
@With(CheckIfCustomer.class)

public class ShoppingCtrl extends Controller {

    // Get a user - if logged in email will be set in the session
	private Customer getCurrentUser() {
		return Customer.getLoggedIn(session().get("email"));
	}
    
    public Result showBasket() {
        Customer customer = Customer.getLoggedIn(session().get("email"));
				        if (customer.basket == null) {
            // If no basket, create one
            customer.basket = new Basket();
            customer.basket.customer = customer;
            customer.update();
        }
        return ok(basket.render(getCurrentUser()));
    }
    
    // Add item to customer basket
    public Result addToBasket(Long id) {
        
        // Find the product
        Product p = Product.find.byId(id);
        
        // Get basket for logged in customer
        Customer customer = Customer.getLoggedIn(session().get("email"));
        
        // Check if item in basket
        if (customer.basket == null) {
            // If no basket, create one
            customer.basket = new Basket();
            customer.basket.customer = customer;
            customer.update();
        }
        // Add product to the basket and save
        customer.basket.addProduct(p);

        customer.update();

        // Show the basket contents     
        return ok(basket.render(customer));
    }
    
    // Add an item to the basket
    public Result addOne(Long itemId) {
        
        // Get the order item
        OrderItem item = OrderItem.find.byId(itemId);
        // Increment quantity
        item.increaseQty();
        // Save
        item.update();

        // Show updated basket
        return redirect(controllers.routes.ShoppingCtrl.showBasket());
    }
    
    public Result removeOne(Long itemId) {
        
        // Get the order item
        OrderItem item = OrderItem.find.byId(itemId);
        // Get user
        Customer c = getCurrentUser();
        // Call basket remove item method
        c.basket.removeItem(item);
        c.basket.update();

        return ok(basket.render(c));

    }

    public Result addOneCard(Long itemId) {
        
        // Get the order item
        OrderItem item = OrderItem.find.byId(itemId);
        // Increment quantity
        item.increaseQty();
        // Save
        item.update();

        // Show updated basket
        return redirect(controllers.routes.ShoppingCtrl.creditCard());
    }

    public Result removeOneCard(Long itemId) {
       
        // Get the order item
        OrderItem item = OrderItem.find.byId(itemId);
        // Get user
        Customer c = getCurrentUser();
        // Call basket remove item method
        c.basket.removeItem(item);
        c.basket.update();

        return ok(creditCard.render(User.getLoggedIn(session().get("email")), c));	
    }

    // Empty Basket
    public Result emptyBasket() {
        
        Customer c = getCurrentUser();
        c.basket.removeAllItems();
        c.basket.update();
        return ok(basket.render(c));
    }
    
    public Result placeOrder() {
        Customer c = getCurrentUser();

        // Create an order instance
        ShopOrder order = new ShopOrder();

        // Associate order with customer
        order.customer = c;
        
        // Copy basket to order
        order.items = c.basket.basketItems;
 
        // Save the order now to generate a new id for this order
        order.save();

				try(FileWriter fw = new FileWriter("OrderHistory", true); //log order history to /txt
    			BufferedWriter bw = new BufferedWriter(fw);
    			PrintWriter out = new PrintWriter(bw))
				{
		    	out.println("Order ID: " + order.id + "," + order.customer.email + " Date: " + order.getDate());

				} catch (IOException e) {

				}	    
       // Move items from basket to order
        for (OrderItem i: order.items) {
            // Associate with order
            i.order = order;
            // Remove from basket
            i.basket = null;
            // update item
						i.product.stock -= i.quantity;
            i.update();
						i.product.update();

				try(FileWriter fw = new FileWriter("OrderHistory", true);
    			BufferedWriter bw = new BufferedWriter(fw);
    			PrintWriter out = new PrintWriter(bw))
				{
		    	out.println(i.product.name + "(" + i.quantity + ") Item price: €" + i.price + ", Total Price: €" + i.getItemTotal());

				} catch (IOException e) {

				}	

        }//end of loop

				try(FileWriter fw = new FileWriter("OrderHistory", true);
    			BufferedWriter bw = new BufferedWriter(fw);
    			PrintWriter out = new PrintWriter(bw))
				{
		    	out.println("Total(Incl Delivery): €" + order.getOrderVatTotal() +"\n");

				} catch (IOException e) {

				}	
       
        // Update the order
        order.update();

        // Clear and update the shopping basket
        c.basket.basketItems = null;
        c.basket.update();
				
        // Show order confirmed view
        return ok(orderConfirmed.render(c, order));
    }
 
    // View an individual order
    public Result viewOrder(long id) {
        ShopOrder order = ShopOrder.find.byId(id);
        return ok(orderConfirmed.render(getCurrentUser(), order));
    }

		public Result creditCard(){
        Customer customer = Customer.getLoggedIn(session().get("email"));
		return ok(creditCard.render(User.getLoggedIn(session().get("email")), customer));		
		}

		public Result addCardSubmit() {
			return redirect(controllers.security.routes.LoginCtrl.login());
    }

		public Result contact() {
        return ok(contact.render(User.getLoggedIn(session().get("email"))));
    }
}
