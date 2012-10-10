package ru.gopnikgame.ai;

public class SimpleLogicStringAi {
	public static final String NOTHING_HAPPENED = "Ниче не происходит";
	public static final String WALK = "w";

	public static String getReply(String in) {
		String ret = NOTHING_HAPPENED;

		if (WALK.equals(in)) {
			ret = "Гуляем ёпта";
		}

		return ret;
	}
}
