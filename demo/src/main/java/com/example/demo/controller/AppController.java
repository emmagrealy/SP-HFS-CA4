package com.example.demo.controller;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.example.demo.allCustomersIterator.CustomerList;
import com.example.demo.allCustomersIterator.Iterator;
import com.example.demo.cardValidationTemplateMethod.AbstractCardValidator;
import com.example.demo.cardValidationTemplateMethod.MastercardValidation;
import com.example.demo.cardValidationTemplateMethod.VisaValidation;
import com.example.demo.item.StockItem;
import com.example.demo.item.StockItemService;
import com.example.demo.loyaltyCardsStrategy.BasicCard;
import com.example.demo.loyaltyCardsStrategy.LoyaltyCard;
import com.example.demo.loyaltyCardsStrategy.NoCard;
import com.example.demo.loyaltyCardsStrategy.PremiumCard;
import com.example.demo.order.ItemOrders;
import com.example.demo.order.ItemOrdersService;
import com.example.demo.user.Customer;
import com.example.demo.user.CustomerServices;

@Controller
public class AppController {

	private ArrayList<StockItem> cart = new ArrayList<StockItem>();

	@Autowired
	private CustomerServices custService;

	@Autowired
	private StockItemService stockService;

	@Autowired
	private ItemOrdersService orderService;

	@RequestMapping("/welcomePage")
	public String welcome() {
		return "welcomePage";
	}

	@RequestMapping("/successPage")
	public String success() {
		return "successPage";
	}

	@RequestMapping("/logout")
	public String logout(HttpSession session) {
		cart.clear();
		session.invalidate();
		return "welcomePage";
	}

	@RequestMapping("/addProduct")
	public String addProd() {
		return "addProduct";
	}

	@RequestMapping("/searchProducts")
	public String searchProducts() {
		return "searchProducts";
	}

	@RequestMapping("/purchase")
	public String purchase() {
		return "purchasePage";
	}
	
	@RequestMapping("/customerDetails")
	public String customerDetails(HttpSession session) {
		final ArrayList<Customer> customers;
		customers = (ArrayList<Customer>) custService.getAllCustomers();
		CustomerList namesRepository = new CustomerList(customers);
		
		ArrayList<Customer> listAll = new ArrayList<Customer>();
		for (Iterator iter = namesRepository.getIterator(); iter.hasNext();) {
			Customer name = (Customer) iter.next();
			int id = name.getUserId();
			String fName = name.getFirstName();
			String lName = name.getLastName();
			Customer c1 = new Customer(id, fName, lName);
			listAll.add(c1);
		}
		session.setAttribute("allCust", listAll);
		return "customerDetails";
	}

	@RequestMapping("/myCart")
	public String myCart(@SessionAttribute("customer") Customer c,HttpSession session, HttpServletRequest request) {
		int count = 0;
		double price = 0.0;
		for (StockItem currItem : cart) {
			count = count + 1;
			double prodPrice = currItem.getPrice();
			price = price + prodPrice;
		}
		
		String loyaltyCard = c.getLoyaltyCard();
		if (loyaltyCard.equals("Basic")) {
			LoyaltyCard lc = new BasicCard();
			double discount = lc.applyTheDiscount();
			double totalPrice = price - (price * discount);
			session.setAttribute("totalPrice", totalPrice);
		}
		else if (loyaltyCard.equals("Premium")) {
			LoyaltyCard lc = new PremiumCard();
			double discount = lc.applyTheDiscount();
			double totalPrice = price - (price * discount);
			session.setAttribute("totalPrice", totalPrice);
		}
		else {
			LoyaltyCard lc = new NoCard();
			double discount = lc.applyTheDiscount();
			double totalPrice = price - (price * discount);
			session.setAttribute("totalPrice", totalPrice);
		}
		
		session.setAttribute("quantity", count);
		session.setAttribute("subTotal", price);
		return "myCart";
	}

	@PostMapping("/addUser")
	public String registerUser(HttpServletRequest request, HttpSession session) {
		String fName = request.getParameter("firstName");
		String lName = request.getParameter("lastName");
		String dob = request.getParameter("dob");
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String shipAdd = request.getParameter("shippingAddress");
		String pay = request.getParameter("paymentMethod");
		String loyaltyCard = request.getParameter("lc");

		Customer c = new Customer(fName, lName, dob, username, password, shipAdd, pay, loyaltyCard);
		custService.addCustomer(c);
		session.setAttribute("customer", c);
		session.setAttribute("loyalty", loyaltyCard);

		return "successPage";
	}

	@RequestMapping("/login")
	public String loginUser(@ModelAttribute Customer c, HttpServletRequest request, HttpSession session) {
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		
		if (custService.getUserByUsernameAndPassword(username, password) != null) {
			c = custService.getUserByUsernameAndPassword(username, password);
			session.setAttribute("customer", c);
			session.setAttribute("loyalty", c.getLoyaltyCard());	
			return "successPage";
		} else if (username.equalsIgnoreCase("Admin") && password.equalsIgnoreCase("Admin123")) {
			session.setAttribute("admin", username);
			return "adminSuccess";

		} else {
			request.setAttribute("error", "Invalid Username or Password");
			return "welcomePage";
		}
	}

	@PostMapping("/addStockItem")
	public String addStock(HttpServletRequest request) {
		String title = request.getParameter("title");
		String manu = request.getParameter("manufacturer");
		double price = Double.parseDouble(request.getParameter("price"));
		String category = request.getParameter("category");
		String image = request.getParameter("image");
		int quantity = Integer.parseInt(request.getParameter("quantity"));

		StockItem si = new StockItem(title, manu, price, category, image, quantity);
		stockService.addItem(si);

		return "adminSuccess";
	}

	@RequestMapping("/search")
	public String searchQ(@RequestParam("searchQ") String searchQ, HttpServletRequest request, HttpSession session) {
		if (request.getParameter("category") != null) {
			String type = "category";
			session.setAttribute("searchBy", type);
			session.setAttribute("query", searchQ);
			return "searchResults";

		} else if (request.getParameter("manufacturer") != null) {
			String type = "manufacturer";
			session.setAttribute("searchBy", type);
			session.setAttribute("query", searchQ);
			return "searchResults";

		} else if (request.getParameter("title") != null) {
			String type = "title";
			session.setAttribute("searchBy", type);
			session.setAttribute("query", searchQ);
			return "searchResults";

		} else {
			return "searchProducts";
		}
	}

	@RequestMapping("/addToCart")
	public String addCart(HttpServletRequest request) {
		int id = Integer.parseInt(request.getParameter("itemId"));
		StockItem newItem = stockService.getItemById(id);

		cart.add(newItem);
		
		newItem.setQuantity(newItem.getQuantity() -1);

		HttpSession session = request.getSession();
		session.setAttribute("list", cart);

		return "successPage";
	}

	@PostMapping("/completePurchase")
	public String completePurchase(@SessionAttribute("customer") Customer c, HttpServletRequest request,
			HttpSession session) {

		String cardName = request.getParameter("cardName");
		String cardNumber = request.getParameter("cardNumber");
		int expiryDateMonth = Integer.parseInt(request.getParameter("expiryDateMonth"));
		int expiryDateYear = Integer.parseInt(request.getParameter("expiryDateYear"));
		String cvv = request.getParameter("cvv");

		boolean result = false;
		AbstractCardValidator validator = null;

		String cardType = request.getParameter("cardType");
		if (cardType.equals("Visa Card")) {
			validator = new VisaValidation(AppController.this, cardName, cardNumber, expiryDateMonth, 
					expiryDateYear, cvv);

		} else if (cardType.equals("MasterCard")) {
			validator = new MastercardValidation(AppController.this, cardName, cardNumber, expiryDateMonth,
					expiryDateYear, cvv);
		}

		result = validator.validate();

		if (!result) {
			request.setAttribute("error", "Invalid Card Details");
			return "purchasePage";
			
		} else {
			double totalPrice = Double.parseDouble(request.getParameter("tp"));
			ItemOrders newOrder = new ItemOrders(totalPrice);
			orderService.addOrder(newOrder);
			ItemOrders newOrder1 = orderService.getOrderById(newOrder.getOrderId());
			
			for (StockItem s : cart) {
				newOrder1.getProducts().add(s);
			}
			orderService.updateOrder(newOrder1.getOrderId(), newOrder1);

			int customerId = c.getUserId();
			Customer newCustomer = custService.getCustomerById(customerId);
			newCustomer.getUserOrders().add(newOrder1);
			custService.updateCustomer(customerId, newCustomer);

			cart.clear();
			return "successPage";
		}
	}

}