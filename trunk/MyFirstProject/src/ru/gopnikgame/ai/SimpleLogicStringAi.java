package ru.gopnikgame.ai;

public class SimpleLogicStringAi {
	public static final String NOTHING_HAPPENED = "���� �� ����������";
	public static final String WALK = "w";

	public static String getReply(String in) {
		String ret = NOTHING_HAPPENED;

		if (WALK.equals(in)) {
			ret = "������ ����";
		}

		return ret;
	}
}
