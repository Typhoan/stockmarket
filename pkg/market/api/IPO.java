package pkg.market.api;
import pkg.exception.StockMarketExpection;
import pkg.market.Market;
import pkg.stock.Stock;

public class IPO {
	public static void enterNewStock(Market m, String symbol, String name,
			double ipoPrice) {
		Stock stock = new Stock(symbol, name, ipoPrice);
		try {
			m.addStock(stock);
		} catch (StockMarketExpection e) {
			e.printStackTrace();
		}
	}
}
