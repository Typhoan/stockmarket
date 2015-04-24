package pkg.market.api;
import java.util.ArrayList;
import java.util.List;

import pkg.exception.StockMarketExpection;
import pkg.market.Market;
import pkg.stock.Stock;

public class PriceSetter implements ISubject {
	List<IObserver> observersList;
	Stock updatedStock;

	@Override
	public void registerObserver(IObserver observer) {
		if (observersList == null) {
			observersList = new ArrayList<IObserver>();
		}
		observersList.add(observer);
	}

	@Override
	public void notifyObserver() {
		for (IObserver observer : observersList) {
			observer.update();
		}
	}

	@Override
	public Object getUpdate() {
		return updatedStock;
	}

	@Override
	public void unRegisterObserver(IObserver observer) {
		observersList.remove(observer);
	}

	public void setNewPrice(Market m, String symbol, double newPrice) {
		try {
			m.updateStockPrice(symbol, newPrice);
		} catch (StockMarketExpection e) {
			e.printStackTrace();
		}
		this.updatedStock = m.getStockForSymbol(symbol);
		if (this.updatedStock != null)
			notifyObserver();
	}

}
