package pkg.order;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Comparator;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import pkg.exception.StockMarketExpection;
import pkg.market.Market;
import pkg.market.api.PriceSetter;
import pkg.util.OrderUtility;

public class OrderBook {
	Market market;
	HashMap<String, ArrayList<Order>> buyOrders;
	HashMap<String, ArrayList<Order>> sellOrders;

	public OrderBook(Market m) {
		this.market = m;
		buyOrders = new HashMap<String, ArrayList<Order>>();
		sellOrders = new HashMap<String, ArrayList<Order>>();
	}

	public void addToOrderBook(Order order) {

		if (order instanceof BuyOrder) {
			if (buyOrders.containsKey(order.getStockSymbol())) {
				buyOrders.get(order.getStockSymbol()).add(order);
			}
			else {
				ArrayList<Order> buyOrderList = new ArrayList<Order>();
				buyOrderList.add(order);
				buyOrders.put(order.getStockSymbol(), buyOrderList);
			}
		}
		else {
			if (sellOrders.containsKey(order.getStockSymbol())) {
				sellOrders.get(order.getStockSymbol()).add(order);
			}
			else {
				ArrayList<Order> sellOrderlist = new ArrayList<Order>();
				sellOrderlist.add(order);
				sellOrders.put(order.getStockSymbol(), sellOrderlist);
			}
		}
	}

	public void trade() {

		for(Entry<String, ArrayList<Order>> buyList : buyOrders.entrySet()) {
			for ( Entry<String, ArrayList<Order>> sellList : sellOrders.entrySet()) {
				if (buyList.getKey() == sellList.getKey()) {
					ArrayList<Order> buySorted = buyOrderSort(buyList.getValue());
					ArrayList<Order> sellSorted = sellOrderSort(sellList.getValue());
					
					int buyNum = 0;
					int sellNum = 0;
					double price = 0.0;
					int sellVolume = 0;
					
					ArrayList<Double> values = findMarketPrice(buySorted, sellSorted);
					
					buyNum = values.get(0).intValue();
					sellNum = values.get(1).intValue();
					price = values.get(2);
					sellVolume = values.get(3).intValue();
					
					if (buyNum != -1 && sellNum != -1 && price != -1.0){
						PriceSetter set = new PriceSetter();
        				set.registerObserver(m.getMarketHistory());
        				m.getMarketHistory().setSubject(set);
        				set.setNewPrice(market, (String)buyList.getKey(), price);
        				
        				performBuyOrderTrade(buySorted, buyNum, price,
								sellVolume);
        				
        				performSellOrderTrade(sellSorted, sellNum, price);
					}
				}
			}
		}
	}

	private void performSellOrderTrade(ArrayList<Order> sellSorted,
			int sellNum, double price) {
		for (int i = 0; i <= sellNum; i++) {
			Order order = sellSorted.get(i);
			sellOrders.get(order.getStockSymbol()).remove(order);
			try {
				order.getTrader().tradePerformed(order, price);
			} catch (StockMarketExpection e) {
				e.printStackTrace();
			}
		}
	}

	private void performBuyOrderTrade(ArrayList<Order> buySorted, int buyNum,
			double price, int sellVolume) {
		for (int i = 0; i <= buyNum; i++) {
			Order buyOrder = buySorted.get(i);
			if (buyOrder.getSize() <= sellVolume) {
				buyOrders.get(buyOrder.getStockSymbol()).remove(buyOrder);
				sellVolume -= buyOrder.getSize();
			} else {
				buyOrder.setSize(buyOrder.getSize() - sellVolume);
			}
			
			try {
				buyOrder.getTrader().tradePerformed(buyOrder, price);
			} catch (StockMarketExpection e) {
				e.printStackTrace();
			}
		}
	}
	
  	//variables used to calculate the  total sell volume and price.
        //default values are given to ensure the maximum amount of trading is done.
	private ArrayList<Double> findMarketPrice(ArrayList<Order> buyList, ArrayList<Order> sellList) {
		ArrayList<Double> marketTradeInformation = new ArrayList<Double>();
       
		int buyVolume = 0;
		int sellVolume = 0;
		int finalBuyVolume = 10000000;
		int finalSellVolume = 0;
		int totalSellVolume = -1;
		int buyNumber = -1, sellNumber = -1;
		double price = -1.0;
		
		for (int i = 0; i < buyList.size(); i++){
			buyVolume += buyList.get(i).getSize();
			sellVolume = 0;
			for (int j = 0; j < sellList.size(); j++) {
				sellVolume += sellList.get(j).getSize();
				if (buyVolume >= sellVolume) {
					if ((buyVolume - sellVolume) <= (finalBuyVolume - finalSellVolume) && totalSellVolume < sellVolume && buyList.get(i).getPrice() >= sellList.get(j).getPrice()) {
						buyNumber = i;
						sellNumber = j;
						finalBuyVolume = buyVolume;
						finalSellVolume = sellVolume;
						price = sellList.get(sellNumber).getPrice();
						totalSellVolume = sellVolume;
					}
				}
			}
		}
		marketTradeInformation.add((double) buyNumber);
		marketTradeInformation.add((double) sellNumber);
		marketTradeInformation.add(price);
		marketTradeInformation.add((double) totalSellVolume);
		return values;
	}
	
	private ArrayList<Order> buyOrderSort(ArrayList<Order> buyOrders){
		ArrayList<Order> sorted = new ArrayList<Order>();
		ArrayList<Order> unsorted = (ArrayList<Order>) buyOrders.clone();
		
		for (int i = 0; i < buyOrders.size(); i++){
			if(buyOrders.get(i).getPrice() == 0){
				sorted.add(buyOrders.get(i));
				unsorted.remove(i);
			}
		}
		
		Collections.sort(unsorted, new Comparator<Order>() {
			@Override
			public int compare(Order o1, Order o2){
				return Double.compare(o2.getPrice(), o1.getPrice());
			}
		});
		

		sorted.addAll(unsorted);
		
		return sorted;
	}
	private ArrayList<Order> sellOrderSort(ArrayList<Order> buyOrders){
		ArrayList<Order> sorted = (ArrayList<Order>) buyOrders.clone();
		Collections.sort(sorted, new Comparator<Order>() {
			@Override
			public int compare(Order o1, Order o2){
				return Double.compare(o1.getPrice(), o2.getPrice());
			}
		});
	
		return sorted;
	}

}
