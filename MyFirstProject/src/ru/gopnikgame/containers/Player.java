package ru.gopnikgame.containers;

public class Player {
	private int currentHP;
	private int maxHP;
	private Damage damage;
	private int lvl;
	private long exp;

	public Player(int maxHP, Damage damage) {
		this.currentHP = maxHP;
		this.maxHP = maxHP;
		this.damage = damage;
		this.lvl = 0;
		this.exp = 0;
	}
}
