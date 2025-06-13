package me.loovcik.minimalPrice.prices;

import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

public class Price
{
	private final Map<String, Double> enchantments = new HashMap<>();

	public double base;
	public Pair<String, Double> trim;
	public Map<String, Double> getEnchantments() { return enchantments; }
	public Pair<String, Double> trimMaterial;

	/** Oblicza finalną cenę */
	public double get() {
		double result = base;
		if (trim != null) result += trim.getValue();
		if (!getEnchantments().isEmpty()){
			for(Map.Entry<String, Double> entry : getEnchantments().entrySet())
				result += entry.getValue();
		}
		if (trimMaterial != null) result += trimMaterial.getValue();
		return result;
	}

	/** Sumuje wartość zaklęć */
	public double sumEnchantments(){
		double result = 0;
		for(Map.Entry<String, Double> entry : getEnchantments().entrySet())
			result += entry.getValue();
		return result;
	}

	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder
				.append("Price={base=")
				.append(base)
				.append(",")
				.append("enchantments={count=")
				.append(getEnchantments().size())
				.append(", price=$")
				.append(sumEnchantments())
				.append("}, ");
		if (trim != null)
		{
			builder.append("trim={name=")
					.append(trim.getKey())
					.append(", price=$")
					.append(trim.getValue())
					.append("}, ");
			if (trimMaterial != null)
				builder.append("trimMaterial={name=").append(trimMaterial.getKey()).append(", price=$").append(trimMaterial.getValue()).append("}, ");
			builder.append("price=$")
					.append(get())
					.append("}");
		}
		return builder.toString();
	}
}