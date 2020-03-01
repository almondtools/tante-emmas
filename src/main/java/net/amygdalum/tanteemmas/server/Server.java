package net.amygdalum.tanteemmas.server;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.templ.handlebars.HandlebarsTemplateEngine;
import net.amygdalum.tanteemmas.external.SimulatedDateSource;
import net.amygdalum.tanteemmas.external.SimulatedDaytimeSource;
import net.amygdalum.tanteemmas.external.SimulatedWeatherSource;
import net.amygdalum.tanteemmas.external.TimeProvider;
import net.amygdalum.tanteemmas.sources.DateSource;
import net.amygdalum.tanteemmas.sources.DaytimeSource;
import net.amygdalum.tanteemmas.sources.WeatherSource;

public class Server extends AbstractVerticle {

	private CustomerRepo customers;
	private ProductRepo products;
	private TemplateEngine engine;

	private TimeProvider time;
	private DateSource date;
	private DaytimeSource daytime;
	private WeatherSource weather;

	public Server() {
		products = new ProductRepo().init();
		customers = new CustomerRepo().init();
		time = new TimeProvider();
		date = new SimulatedDateSource(time);
		daytime = new SimulatedDaytimeSource(time);
		weather = new SimulatedWeatherSource(time, date);
	}

	@SuppressWarnings("deprecation")
	public void start() {
		Router router = Router.router(vertx);
		engine = HandlebarsTemplateEngine.create(vertx).setExtension("html");
		router.route("/speed/:speed").handler(this::speed);
		router.route("/login").handler(this::login);
		router.route("/logout").handler(this::logout);
		router.route("/order/:product").handler(this::order);
		router.route("/prices").handler(this::prices);
		router.route("/showPrices").handler(this::showPrices);
		router.route("/showLogin").handler(this::showLogin);
		router.route().handler(this::show);

		HttpServer server = vertx.createHttpServer();
		server.requestHandler(router).listen(8080);
	}

	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new Server());
	}

	public void speed(RoutingContext context) {
		long speed = Long.parseLong(context.request().getParam("speed"));
		time.setSpeed(speed);
		context.reroute("/prices");
	}

	public void login(RoutingContext context) {
		String name = context.request().getParam("customer");
		Customer customer = customers.getCustomer(name);
		PriceCalculator.customer = customer;
		context.reroute("/prices");
	}

	public void logout(RoutingContext context) {
		PriceCalculator.customer = null;
		context.reroute("/showLogin");
	}

	public void order(RoutingContext context) {
		if (PriceCalculator.customer == null) {
			context.next();
			return;
		}
		PriceCalculator prices = new PriceCalculator(date, daytime, weather);
		String name = context.request().getParam("product");
		Map<String, Object> product = products.getProduct(name);
		String date = DateFormat.getInstance().format(new Date(time.millis()));
		prices.order(date, product);
		context.reroute("/prices");
	}

	public void prices(RoutingContext context) {
		if (PriceCalculator.customer == null) {
			context.next();
			return;
		}
		try {
			PriceCalculator prices = new PriceCalculator(date, daytime, weather);
			List<Map<String, Object>> priceTable = new ArrayList<>();
			context.data().put("products", priceTable);
			for (Map<String, Object> product : products.getProducts()) {
				BigDecimal price = prices.computePrice(product);

				Map<String, Object> productWithPrice = new HashMap<>(product);
				productWithPrice.put("price", price);
				priceTable.add(productWithPrice);
			}
			context.next();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void show(RoutingContext context) {
		context.data().put("date", date.getDate());
		context.data().put("daytime", daytime.getDaytime());
		context.data().put("weather", weather.getWeather());

		if (PriceCalculator.customer == null) {
			context.reroute("/showLogin");
		} else {
			context.reroute("/showPrices");
		}
	}

	public void showPrices(RoutingContext context) {
		context.data().put("speed", time.getSpeed());
		context.data().put("incspeed", time.getSpeed() * 10);
		context.data().put("decspeed", time.getSpeed() / 10);
		context.data().put("user", PriceCalculator.customer.name);

		engine.render(context.data(), "src/main/resources/index.html", res -> {
			if (res.succeeded()) {
				context.response().putHeader(HttpHeaders.CONTENT_TYPE, "text/html").end(res.result());
			} else {
				context.fail(res.cause());
			}
		});
	}

	public void showLogin(RoutingContext context) {
		engine.render(context.data(), "src/main/resources/login.html", res -> {
			if (res.succeeded()) {
				context.response().putHeader(HttpHeaders.CONTENT_TYPE, "text/html").end(res.result());
			} else {
				context.fail(res.cause());
			}
		});
	}

}
