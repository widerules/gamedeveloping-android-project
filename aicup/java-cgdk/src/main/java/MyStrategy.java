import model.*;

import java.util.Random;
import java.util.TreeSet;

public final class MyStrategy implements Strategy {
    private final Random random = new Random();

    private static Trooper primary = null;

    private static int a = 0;

    private static Trooper medic = null;
    private static Move medicMove = null;

    private static int soldierDestX = 0;
    private static int soldierDestY = 0;

    private static Node pathNode = null;


    @Override
    public void move(Trooper self, World world, Game game, Move move) {
        System.out.println("Start for: " + self.getType() + " with ap:" + self.getActionPoints());
//        System.out.println("test a "+a++);

        PathFinder pf = new PathFinder(world);

        if (self.getType().equals(TrooperType.FIELD_MEDIC)) {
            medicTactic(self, world, game, move, pf);
            medic = self;
            medicMove = move;
            return;
        }

        if (self.getType().equals(TrooperType.SOLDIER)) {
            soldierTactic(self, world, game, move, pf);
            return;
        }

        if (self.getType().equals(TrooperType.COMMANDER)) {
            commanderTactic(self, world, game, move, pf);
            return;
        }


        Trooper enemy = getEnemy(self, world, game, move);
        if (null != enemy) {
            if (self.getActionPoints() >= self.getShootCost()) {
                move.setAction(ActionType.SHOOT);
                move.setX(enemy.getX());
                move.setY(enemy.getY());
                return;
            }
        }


        if (self.getActionPoints() < game.getStandingMoveCost()) {
            return;
        }
        move.setAction(ActionType.MOVE);
        move.setDirection(pf.getDirection(self, world, 15, 10, 3));
    }


    private static int stepsToGo;
    private static int resqueX;
    private static int resqueY;
    public void toTheRescue(Trooper self, World world, Game game, Move move, PathFinder pf) {
        if (stepsToGo>0&&self.getActionPoints()>=game.getStandingMoveCost()){
            for (Trooper t : world.getTroopers()){
                if (!t.isTeammate()){
                    resqueX = random.nextInt(30);
                    resqueY = random.nextInt(20);
                }
            }

            move.setAction(ActionType.MOVE);
            move.setDirection(pf.getDirection(self,world,resqueX,resqueY,3));
            stepsToGo--;
            return;
        }
    }




    public Trooper getEnemy(Trooper self, World world, Game game, Move move) {
        for (Trooper t : world.getTroopers()) {
            if (!t.isTeammate()) {
                if (world.isVisible(self.getShootingRange(), self.getX(), self.getY(), self.getStance(),
                        t.getX(), t.getY(), t.getStance())) {
                    return t;
                }
            }
        }

        return null;
    }

    private boolean followSoldier(Trooper self, World world, Game game, Move move) {
        Trooper soldier = getSoldier(self, world, game, move);
        if (null != soldier && soldier.getHitpoints() > 1) {
            if (self.getActionPoints() >= game.getStandingMoveCost()) {
                move.setAction(ActionType.MOVE);
                PathFinder pf = new PathFinder(world);
                Direction direction = pf.getDirection(self, world, soldier.getX(), soldier.getY(), 2);
                if (direction.equals(Direction.CURRENT_POINT)){
                    Bonus bonus = getNearestBonus(self, world, self.isHoldingMedikit(), self.isHoldingGrenade(), self.isHoldingFieldRation());
                    if (null != bonus && isBonusReachable(bonus, world)) {
                        System.out.println("trying to collect bonus");
                        direction = pf.getDirection(self, world, bonus.getX(), bonus.getY(), 0);
                    }
                }
                move.setDirection(direction);
                return true;


            }
        }

        return false;
    }


    private void commanderTactic(Trooper self, World world, Game game, Move move, PathFinder pf) {
//        System.out.println(self.getDistanceTo(self.getX() + 1, self.getY() + 1));

//        ViewAnalyzer va = new ViewAnalyzer(world);
//        va.analyze(self, world, game, move);

        if (self.getHitpoints() < 71 && self.isHoldingMedikit()) {
            if (self.getActionPoints() >= game.getMedikitUseCost()) {
                System.out.println("use medikit");
                move.setAction(ActionType.USE_MEDIKIT);
                move.setDirection(Direction.CURRENT_POINT);
                return;
            }
        }
//        if (self.getActionPoints() < game.getStandingMoveCost()){
//            move.setAction(ActionType.REQUEST_ENEMY_DISPOSITION);
//            move.setDirection(Direction.CURRENT_POINT);
//            return;
//        }

//        if (primary==null || primary.getHitpoints()<1){
//            System.out.println("check primary");
//            primary =getEnemy(self, world, game, move);
//        }
//        Trooper enemy = primary!=null&&self.getDistanceTo(primary)<=self.getShootingRange()?primary:getEnemy(self, world, game, move);

        Trooper enemy = getEnemy(self, world, game, move);



        if (null != enemy && enemy.getHitpoints() > 0) {
            if (self.getActionPoints() >= game.getGrenadeThrowCost() && self.isHoldingGrenade()){
                if( self.getDistanceTo(enemy) < game.getGrenadeThrowRange()) {
                    System.out.println("grenade!" + game.getGrenadeThrowCost());
                    move.setAction(ActionType.THROW_GRENADE);
                    move.setX(enemy.getX());
                    move.setY(enemy.getY());
                    return;
                }else if (self.getStance().equals(TrooperStance.STANDING)){
                    System.out.println("come close");
                    move.setAction(ActionType.MOVE);
                    move.setDirection(pf.getDirection(self, world, enemy.getX(), enemy.getY(), 5));
                    return;
                }
            }

//            if (!self.getStance().equals(TrooperStance.PRONE)
//                && self.getActionPoints()>=game.getStanceChangeCost()){
//                System.out.println("get low");
//                move.setAction(ActionType.LOWER_STANCE);
//                move.setDirection(Direction.CURRENT_POINT);
//                return;
//            }
            if (self.getActionPoints() >= self.getShootCost()) {
                System.out.println("try shoot");
                move.setAction(ActionType.SHOOT);
                move.setX(enemy.getX());
                move.setY(enemy.getY());
                return;
            }
        }

        if (self.getActionPoints() >= game.getFieldRationEatCost() && self.isHoldingFieldRation()) {
            System.out.println("eat ration");
            move.setAction(ActionType.EAT_FIELD_RATION);
            move.setDirection(Direction.CURRENT_POINT);
            return;
        }

//        if (!self.getStance().equals(TrooperStance.STANDING)
//                && self.getActionPoints()>=game.getStanceChangeCost()){
//            System.out.println("get up");
//            move.setAction(ActionType.RAISE_STANCE);
//            move.setDirection(Direction.CURRENT_POINT);
//            return;
//        }

        //выключено чтобы не запирать солдата в туннелях
        if (followSoldier(self, world, game, move)){
            System.out.println("follow");
            return;
        }


        if (self.getActionPoints() >= game.getStandingMoveCost()) {
            move.setAction(ActionType.MOVE);
            Bonus bonus = getNearestBonus(self, world, self.isHoldingMedikit(), self.isHoldingGrenade(), self.isHoldingFieldRation());
            if (null != bonus && isBonusReachable(bonus, world)) {
                System.out.println("trying to collect bonus");
                move.setDirection(pf.getDirection(self, world, bonus.getX(), bonus.getY(), 0));
                return;
            }

            System.out.println("try move");
            move.setDirection(getCWDirection(self, world, game, move, pf));
            return;
        }
    }

    private void soldierTactic(Trooper self, World world, Game game, Move move, PathFinder pf) {
        if (self.getHitpoints() < 71 && self.isHoldingMedikit()) {
            if (self.getActionPoints() >= game.getMedikitUseCost()) {
                System.out.println("use medikit");
                move.setAction(ActionType.USE_MEDIKIT);
                move.setDirection(Direction.CURRENT_POINT);
                return;
            }
        }

//        if (primary==null || primary.getHitpoints()<1){
//            System.out.println("check primary");
//            primary = getEnemy(self, world, game, move);
//        }

//        Trooper enemy = primary!=null&&self.getDistanceTo(primary)<=self.getShootingRange()?primary:getEnemy(self, world, game, move);
        Trooper enemy = getEnemy(self, world, game, move);
        if (null != enemy && enemy.getHitpoints() > 0) {
//            System.out.println("Enemy: "+enemy.isHoldingMedikit()+ " "+enemy.isHoldingFieldRation()+" "+enemy.getHitpoints());
//            System.out.println("killing enemy");
            if (self.getActionPoints() >= game.getGrenadeThrowCost() && self.isHoldingGrenade()){
                if( self.getDistanceTo(enemy) < game.getGrenadeThrowRange()) {
                    System.out.println("grenade!" + game.getGrenadeThrowCost());
                    move.setAction(ActionType.THROW_GRENADE);
                    move.setX(enemy.getX());
                    move.setY(enemy.getY());
                    return;
                }else{
                    System.out.println("come close");
                    move.setAction(ActionType.MOVE);
                    move.setDirection(pf.getDirection(self, world, enemy.getX(), enemy.getY(), 5));
                    return;
                }
            }
            if (self.getActionPoints() >= self.getShootCost()) {
                System.out.println("shoot!");
                move.setAction(ActionType.SHOOT);
                move.setX(enemy.getX());
                move.setY(enemy.getY());
                return;
            }
        }


        if (self.getActionPoints() >= game.getFieldRationEatCost() && self.isHoldingFieldRation()) {
            System.out.println("eat ration");
            move.setAction(ActionType.EAT_FIELD_RATION);
            move.setDirection(Direction.CURRENT_POINT);
            return;
        }


        if (self.getActionPoints() < game.getStandingMoveCost()) {
            System.out.println("exit");
            return;
        }
        move.setAction(ActionType.MOVE);

        Bonus bonus = getNearestBonus(self, world, self.isHoldingMedikit(), self.isHoldingGrenade(), self.isHoldingFieldRation());
        if (null != bonus && isBonusReachable(bonus, world)) {
            System.out.println("trying to collect bonus");
            move.setDirection(pf.getDirection(self, world, bonus.getX(), bonus.getY(), 0));
        } else {
            System.out.println("move");
//            move.setDirection(pf.getDirection(self, world, 15, 10, 3));


            Direction direction = Direction.CURRENT_POINT;
            Trooper medic = getMedic(self, world, game, move);
            Trooper commander =getCommander(self, world, game, move);
            if (medic!=null&&commander!=null&&self.getDistanceTo(medic)>5 || self.getDistanceTo(commander)>5){
                direction = Direction.CURRENT_POINT;
            }else{
                direction = getCWDirection(self, world, game, move, pf);
            }

//            if (direction.equals(Direction.CURRENT_POINT)){
//                move.setDirection(pf.getDirection(self, world, 15, 10, 3));
//            }else {
//                move.setDirection(getCWDirection(self, world, game, move, pf));
                move.setDirection(direction);
//            }
        }
    }

    private boolean isBonusReachable(Bonus bonus, World world) {
        for (Trooper t : world.getTroopers()) {
            if (t.getX() == bonus.getX() && t.getY() == bonus.getY()) {
                return false;
            }
        }
        return true;
    }

    private Direction getCWDirection(Trooper self, World world, Game game, Move move, PathFinder pf) {
        Direction ret = Direction.CURRENT_POINT;
        if (self.getX() < 20 && self.getY() < 5) {
            System.out.println("trying to move to 29 0");
            ret = pf.getDirection(self, world, 29, 0, 2);
        } else if (self.getX() > 15 && self.getY() < 17) {
            System.out.println("trying to move to 25 19");
            ret =  pf.getDirection(self, world, 25, 19, 2);
        } else if (self.getX() > 5 && self.getY() > 17) {
            System.out.println("trying to move to 0 19");
            ret =  pf.getDirection(self, world, 0, 19, 2);
        } else if (self.getX() < 15 && self.getY() > 5) {
            System.out.println("trying to move to 0 0");
            ret =  pf.getDirection(self, world, 0, 0, 2);
        } else {
            System.out.println("trying to move to 15 10");
            ret =  pf.getDirection(self, world, 15, 10, 3);
        }
        if (ret.equals(Direction.CURRENT_POINT)){
            ret =  pf.getDirection(self, world, random.nextInt(30), random.nextInt(20), 2);
        }
        return ret;
    }


    private Bonus getNearestBonus(final Trooper self, World world, boolean excludeMedikit, boolean excludeGrenade, boolean excludeFieldRation) {
//        System.out.println("try bonus");
        if (world.getBonuses().length == 0 || (excludeMedikit && excludeGrenade && excludeFieldRation)) {
            return null;
        }
        TreeSet<Bonus> bonuses = new TreeSet<>(new BonusComparator(self));
        for (Bonus tmpB : world.getBonuses()) {
            if (tmpB.getType().equals(BonusType.MEDIKIT) && excludeMedikit) continue;
            if (tmpB.getType().equals(BonusType.GRENADE) && excludeGrenade) continue;
            if (tmpB.getType().equals(BonusType.FIELD_RATION) && excludeFieldRation) continue;

            bonuses.add(tmpB);
//            return tmpB;
        }
//        return null;
        return bonuses.isEmpty() ? null : bonuses.pollFirst();
    }


    private void medicTactic(Trooper self, World world, Game game, Move move, PathFinder pf) {
        if  (stepsToGo!=0){
            toTheRescue(self, world, game, move, pf);
            return;
        }

        if (medic!=null && medicMove.getAction().equals(ActionType.HEAL)
                &&medic.getHitpoints()> self.getHitpoints()){
            System.out.println("init resque");
            stepsToGo = 4;
            resqueX = random.nextInt(30);
            resqueY = random.nextInt(20);
            toTheRescue(self, world, game, move, pf);
        }

//        System.out.println("Medic: "+medic.getX() + " "+medic.getY());

        if (self.getHitpoints() < 50 && self.isHoldingMedikit()) {
            if (self.getActionPoints() >= game.getMedikitUseCost()) {
                move.setAction(ActionType.USE_MEDIKIT);
                move.setDirection(Direction.CURRENT_POINT);
                return;
            }
        }

        Trooper enemy = getEnemy(self, world, game, move);

        if (self.getHitpoints() < 75) {
//            if (enemy!=null && enemy.getHitpoints()>0
//                    &&self.getActionPoints() >= game.getFieldMedicHealCost()){
//                move.setAction(ActionType.MOVE);
//                move.setDirection(pf.getDirection(self, world, random.nextInt(30), random.nextInt(20), 5));
//                return;
//            }

            if (self.getActionPoints() >= game.getFieldMedicHealCost()) {
                move.setAction(ActionType.HEAL);
                move.setDirection(Direction.CURRENT_POINT);
                return;
            }
        }

        boolean saveSoldier = false;

        Trooper soldier = getSoldier(self, world, game, move);
        if (null != soldier && soldier.getHitpoints() > 0) {
            if (soldier.getHitpoints() < soldier.getMaximalHitpoints()){
                if(isNear(self, soldier)){
                    if (self.isHoldingMedikit() && self.getActionPoints() >= game.getMedikitUseCost()
                            && soldier.getHitpoints()<70){
                        move.setAction(ActionType.USE_MEDIKIT);
                        move.setX(soldier.getX());
                        move.setY(soldier.getY());
                        return;
                    }
                    if (self.getActionPoints() >= game.getFieldMedicHealCost()) {
                        move.setAction(ActionType.HEAL);
                        move.setX(soldier.getX());
                        move.setY(soldier.getY());
                        return;
                    }
                }
                else{
                    saveSoldier = true;
                }
            }
        }

        boolean saveCommander = false;

        Trooper commander = getCommander(self, world, game, move);
        if (null != commander && commander.getHitpoints() > 0) {
            if (commander.getHitpoints() < commander.getMaximalHitpoints()){
                if (isNear(self, commander)) {
                    if (self.isHoldingMedikit() && self.getActionPoints() >= game.getMedikitUseCost()
                            && commander.getHitpoints() < 70){
                        move.setAction(ActionType.USE_MEDIKIT);
                        move.setX(commander.getX());
                        move.setY(commander.getY());
                        return;
                    }
                    if (self.getActionPoints() >= game.getFieldMedicHealCost()) {
                        move.setAction(ActionType.HEAL);
                        move.setX(commander.getX());
                        move.setY(commander.getY());
                        return;
                    }
                }else{
                    saveCommander = true;
                }
            }
        }

        System.out.println("goto heal commander test: "+saveSoldier + " "+ saveCommander);
        if (null != commander && commander.getHitpoints() > 0 && !saveSoldier && saveCommander) {
            System.out.println("goto heal commander");
            if (self.getActionPoints() >= game.getStandingMoveCost()) {
                move.setAction(ActionType.MOVE);
                move.setDirection(pf.getDirection(self, world, commander.getX(), commander.getY(), 1));
                return;
            }
        }

        if (null != soldier && soldier.getHitpoints() > 0 && saveSoldier) {
            System.out.println("goto heal commander");
            if (self.getActionPoints() >= game.getStandingMoveCost()) {
                move.setAction(ActionType.MOVE);
                move.setDirection(pf.getDirection(self, world, soldier.getX(), soldier.getY(), 1));
                return;
            }
        }



//        if (primary==null||primary.getHitpoints()<1){
//            primary =getEnemy(self, world, game, move);
//        }
//        Trooper enemy = primary!=null&&self.getDistanceTo(primary)<=self.getShootingRange()?primary:getEnemy(self, world, game, move);




        if (null != enemy && enemy.getHitpoints() > 0) {
            if (self.getActionPoints() >= game.getGrenadeThrowCost() && self.isHoldingGrenade()){
                if( self.getDistanceTo(enemy) < game.getGrenadeThrowRange()) {
                    System.out.println("grenade!" + game.getGrenadeThrowCost());
                    move.setAction(ActionType.THROW_GRENADE);
                    move.setX(enemy.getX());
                    move.setY(enemy.getY());
                    return;
                }else{
                    System.out.println("come close");
                    move.setAction(ActionType.MOVE);
                    move.setDirection(pf.getDirection(self, world, enemy.getX(), enemy.getY(), 5));
                    return;
                }
            }

            if (self.getActionPoints() >= self.getShootCost()) {
                move.setAction(ActionType.SHOOT);
                move.setX(enemy.getX());
                move.setY(enemy.getY());
                return;
            }
        }


        if (self.getActionPoints() >= game.getFieldRationEatCost() && self.isHoldingFieldRation()) {
            System.out.println("eat ration");
            move.setAction(ActionType.EAT_FIELD_RATION);
            move.setDirection(Direction.CURRENT_POINT);
            return;
        }

        if (null != soldier && soldier.getHitpoints() > 0) {
            if (self.getActionPoints() >= game.getStandingMoveCost()) {
                move.setAction(ActionType.MOVE);

                move.setDirection(pf.getDirection(self, world, soldier.getX(), soldier.getY(), 1));
                return;
            }
        }

        if (self.getActionPoints() >= game.getStandingMoveCost()) {
            move.setAction(ActionType.MOVE);
            move.setDirection(getCWDirection(self, world, game, move, pf));
//            move.setDirection(pf.getDirection(self, world, 15, 10, 3));
            return;
        }

    }

    private boolean isNear(Trooper t, Trooper t2) {
        return Math.abs(t.getX() - t2.getX()) + Math.abs(t.getY() - t2.getY()) < 2;
    }

    private Trooper getSoldier(Trooper self, World world, Game game, Move move) {
        for (Trooper t : world.getTroopers()) {
            if (t.isTeammate() && t.getType().equals(TrooperType.SOLDIER)) {
                return t;
            }
        }
        return null;
    }

    private Trooper getMedic(Trooper self, World world, Game game, Move move) {
        for (Trooper t : world.getTroopers()) {
            if (t.isTeammate() && t.getType().equals(TrooperType.FIELD_MEDIC)) {
                return t;
            }
        }
        return null;
    }

    private Trooper getCommander(Trooper self, World world, Game game, Move move) {
        for (Trooper t : world.getTroopers()) {
            if (t.isTeammate() && t.getType().equals(TrooperType.COMMANDER)) {
                return t;
            }
        }
        return null;
    }
}
