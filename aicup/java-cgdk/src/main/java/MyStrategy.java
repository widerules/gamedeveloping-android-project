import model.*;

import java.util.*;

public final class MyStrategy implements Strategy {
    private final Random random = new Random();

    private static HashMap<Integer, Integer> previousSteps;
    private static int lastMoveIndex = -1;
    private static HashSet<ActionType> prevActions = null;
    private static Move prevMove = null;
    private boolean stucked = false;

    private static TrooperType primaryType = null;
    private static long primaryId = -1;


    @Override
    public void move(Trooper self, World world, Game game, Move move) {
        if (previousSteps==null){
            previousSteps = new HashMap<>();
            prevActions = new HashSet<>();
        }
        if (lastMoveIndex != world.getMoveIndex()){
            lastMoveIndex = world.getMoveIndex();
            System.out.println(previousSteps.toString());
            previousSteps.clear();
            prevActions.clear();
        }




        System.out.println("");
        System.out.println("Stats: "+prevActions.toString() + "; size"+previousSteps.size());


        System.out.println(self.getType() + " with ap:" + self.getActionPoints());

        PathFinder pf = new PathFinder(world);

        if (self.getType().equals(TrooperType.FIELD_MEDIC)) {
            medicTactic(self, world, game, move, pf);
            return;
        }

        if (self.getType().equals(TrooperType.SOLDIER)) {
            if (prevActions!=null && prevActions.size()==1 && prevActions.contains(ActionType.MOVE)
                    &&previousSteps.size()==2 && self.getActionPoints()<2){
                stucked=true;
            }

            int coordUID = self.getX()*100+self.getY();

            Integer cellInMap = previousSteps.get(coordUID);
            if (cellInMap==null){
                previousSteps.put(coordUID, 1);
            }else{
                previousSteps.put(coordUID, cellInMap+1);
            }
            if (prevMove!=null){
                prevActions.add(prevMove.getAction());
            }
            prevMove = move;

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

    public Trooper getEnemy(Trooper self, World world, Game game, Move move) {
//        primary.getTeammateIndex()

        LinkedList<Trooper> trooperLinkedList = new LinkedList<>();
        for (Trooper t : world.getTroopers()) {
            if (!t.isTeammate()) {
                if (world.isVisible(self.getShootingRange(), self.getX(), self.getY(), self.getStance(),
                        t.getX(), t.getY(), t.getStance())) {
                    trooperLinkedList.add(t);
                }
            }
        }

        for (Trooper t: trooperLinkedList){
            if (t.getHitpoints() <= self.getDamage()){
                return t;
            }
        }

        for (Trooper t: trooperLinkedList){
            if (t.getType().equals(TrooperType.FIELD_MEDIC)){
                return t;
            }
        }

        return trooperLinkedList.isEmpty()?null:trooperLinkedList.get(0);
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
                    }else{
                        direction = pf.getDirection(self,world,random.nextInt(30),random.nextInt(20),10);
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

        if (self.getHitpoints() < 70 && self.isHoldingMedikit()) {
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

        Trooper primary = getPrimary(self, world, game, move);
        if (primary != null){
            enemy = primary;
            if (!world.isVisible(self.getShootingRange(), self.getX(), self.getY(), self.getStance(),
                    primary.getX(), primary.getY(), primary.getStance())){
                System.out.println("come close to primary");
                move.setAction(ActionType.MOVE);
                move.setDirection(pf.getDirectionReachable(self, world, enemy.getX(), enemy.getY(),enemy.getStance(), self.getShootingRange()-0.5));
                return;
            }
        }


        if (null != enemy && enemy.getHitpoints() > 0) {
            if (self.getActionPoints() >= game.getGrenadeThrowCost() && self.isHoldingGrenade()){
                System.out.println("==> "+self.getDistanceTo(enemy));
                if( self.getDistanceTo(enemy) <= game.getGrenadeThrowRange()) {
                    System.out.println("grenade!" + game.getGrenadeThrowCost());
                    move.setAction(ActionType.THROW_GRENADE);
                    move.setX(enemy.getX());
                    move.setY(enemy.getY());
                    primaryId = enemy.getPlayerId();
                    primaryType = enemy.getType();
                    return;
                }else if (self.getActionPoints() >= game.getGrenadeThrowCost()+moveCost(self, game)){
                    System.out.println("come close");
                    move.setAction(ActionType.MOVE);
                    move.setDirection(pf.getDirection(self, world, enemy.getX(), enemy.getY(), game.getGrenadeThrowRange()));
                    return;
                }
            }

//            if (kneelTrooper(self, world, game, move, enemy)) return;
            if (changeStance(self, game, move)) return;

            if (self.getActionPoints() >= self.getShootCost()) {
                System.out.println("try shoot");
                move.setAction(ActionType.SHOOT);
                move.setX(enemy.getX());
                move.setY(enemy.getY());
                primaryId = enemy.getPlayerId();
                primaryType = enemy.getType();
                return;
            }
        }

        if (self.getActionPoints() >= game.getFieldRationEatCost() && self.isHoldingFieldRation()) {
            System.out.println("eat ration");
            move.setAction(ActionType.EAT_FIELD_RATION);
            move.setDirection(Direction.CURRENT_POINT);
            return;
        }

        if (!self.getStance().equals(TrooperStance.STANDING)
                && self.getActionPoints()>=game.getStanceChangeCost()){
            System.out.println("get up");
            move.setAction(ActionType.RAISE_STANCE);
            move.setDirection(Direction.CURRENT_POINT);
            return;
        }

        if (gotoSpottedEnemy(self, world, game, move, pf)) return;

        //выключено чтобы не запирать солдата в туннелях
        if (followSoldier(self, world, game, move)){
            System.out.println("follow");
            return;
        }

//        if (collectBonus(self, world, move, pf)) return;

        if (self.getActionPoints() >= game.getStandingMoveCost()) {
            System.out.println("try move");
            move.setAction(ActionType.MOVE);
            move.setDirection(getCWDirection(self, world, game, move, pf));
            return;
        }
    }

    private boolean gotoSpottedEnemy(Trooper self, World world, Game game, Move move, PathFinder pf) {
//        System.out.println("go to spotted enemy");
        if (self.getActionPoints()>=moveCost(self,game)){
            for (Trooper t : world.getTroopers()){
                if (!t.isTeammate()){
                    System.out.print("enemy spotted! \n");
                    move.setAction(ActionType.MOVE);
                    move.setDirection(pf.getDirectionReachable(self, world, t.getX(), t.getY(), t.getStance(), self.getShootingRange()-1));
                    return true;
                }
            }
        }
        return false;
    }

    private void soldierTactic(Trooper self, World world, Game game, Move move, PathFinder pf) {
        if (self.getHitpoints() < 70 && self.isHoldingMedikit()) {
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


        Trooper primary = getPrimary(self, world, game, move);
        if (primary != null){
            enemy = primary;
            if (!world.isVisible(self.getShootingRange(), self.getX(), self.getY(), self.getStance(),
                    primary.getX(), primary.getY(), primary.getStance())){
                System.out.println("come close to primary");
                move.setAction(ActionType.MOVE);
                move.setDirection(pf.getDirectionReachable(self, world, enemy.getX(), enemy.getY(),enemy.getStance(), self.getShootingRange()-0.5));
                return;
            }
        }



        if (null != enemy && enemy.getHitpoints() > 0) {
//            System.out.println("Enemy: "+enemy.isHoldingMedikit()+ " "+enemy.isHoldingFieldRation()+" "+enemy.getHitpoints());
//            System.out.println("killing enemy");
            if (self.getActionPoints() >= game.getGrenadeThrowCost() && self.isHoldingGrenade()){
                if( self.getDistanceTo(enemy) <= game.getGrenadeThrowRange()) {
                    System.out.println("grenade!" + game.getGrenadeThrowCost());
                    move.setAction(ActionType.THROW_GRENADE);
                    move.setX(enemy.getX());
                    move.setY(enemy.getY());
                    primaryId = enemy.getPlayerId();
                    primaryType = enemy.getType();
                    return;
                }else if (self.getActionPoints() >= game.getGrenadeThrowCost()+moveCost(self, game)){
                    System.out.println("come close");
                    move.setAction(ActionType.MOVE);
                    move.setDirection(pf.getDirection(self, world, enemy.getX(), enemy.getY(), game.getGrenadeThrowRange()));
                    return;
                }
            }

//            if (kneelTrooper(self, world, game, move, enemy)) return;

            if (changeStance(self, game, move)) return;

            if (self.getActionPoints() >= self.getShootCost()) {
                System.out.println("shoot!");
                move.setAction(ActionType.SHOOT);
                move.setX(enemy.getX());
                move.setY(enemy.getY());
                primaryId = enemy.getPlayerId();
                primaryType = enemy.getType();
                return;
            }
        }


        if (self.getActionPoints() >= game.getFieldRationEatCost() && self.isHoldingFieldRation()) {
            System.out.println("eat ration");
            move.setAction(ActionType.EAT_FIELD_RATION);
            move.setDirection(Direction.CURRENT_POINT);
            return;
        }

        if (!self.getStance().equals(TrooperStance.STANDING)
                && self.getActionPoints()>=game.getStanceChangeCost()){
            System.out.println("get up");
            move.setAction(ActionType.RAISE_STANCE);
            move.setDirection(Direction.CURRENT_POINT);
            return;
        }


        if (self.getActionPoints() < game.getStandingMoveCost()) {
            System.out.println("exit");
            return;
        }


        if (gotoSpottedEnemy(self, world, game, move, pf)) return;

        if (stucked && self.getActionPoints() >= moveCost(self, game)){
            move.setAction(ActionType.MOVE);
            if (random.nextBoolean()) {
                move.setDirection(random.nextBoolean() ? Direction.NORTH : Direction.SOUTH);
            } else {
                move.setDirection(random.nextBoolean() ? Direction.WEST : Direction.EAST);
            }
            stucked = false;
            return;
        }

        if (collectBonus(self, world, move, pf)) return;


        System.out.println("move");
        move.setAction(ActionType.MOVE);
        Direction direction = Direction.CURRENT_POINT;
        Trooper medic = getMedic(self, world, game, move);
        Trooper commander =getCommander(self, world, game, move);
        if (medic!=null&&commander!=null&&
                (self.getDistanceTo(medic)>4 || self.getDistanceTo(commander)>4)){
            direction = Direction.CURRENT_POINT;
            if (gotoSpottedEnemy(self, world, game, move, pf)) return;
        }else{
            direction = getCWDirection(self, world, game, move, pf);
        }
        System.out.println("chosen direction: "+direction);

        move.setDirection(direction);

    }

    private boolean changeStance(Trooper self, Game game, Move move) {
        TrooperStance trooperStance = getBestDMGStance(self, game);
        if (!self.getStance().equals(trooperStance)){
            System.out.println("change stance from "+self.getStance()+" to "+trooperStance);
            move.setAction(getAction(self.getStance(), trooperStance));
            move.setDirection(Direction.CURRENT_POINT);
            return true;
        }
        return false;
    }

    private boolean collectBonus(Trooper self, World world, Move move, PathFinder pf) {
        Bonus bonus = getNearestBonus(self, world, self.isHoldingMedikit(), self.isHoldingGrenade(), self.isHoldingFieldRation());
        if (null != bonus && isBonusReachable(bonus, world)) {
            System.out.println("trying to collect bonus");
            move.setAction(ActionType.MOVE);
            move.setDirection(pf.getDirection(self, world, bonus.getX(), bonus.getY(), 0));
            return true;
        }
        return false;
    }

    private boolean kneelTrooper(Trooper self, World world, Game game, Move move, Trooper enemy) {
        if (!self.getStance().equals(TrooperStance.KNEELING)
                && isKneelingBetter(self, game)
                && world.isVisible(self.getShootingRange(), self.getX(), self.getY(), TrooperStance.KNEELING,
                enemy.getX(), enemy.getY(), enemy.getStance())
                ){
            System.out.println("get low");
            move.setAction(ActionType.LOWER_STANCE);
            move.setDirection(Direction.CURRENT_POINT);
            return true;
        }
        return false;
    }

    private int moveCost(Trooper t, Game game){
        switch (t.getStance()){
            case KNEELING:
                return game.getKneelingMoveCost();
            case PRONE:
                return game.getProneMoveCost();
            case STANDING:
                return game.getStandingMoveCost();
        };
        return game.getProneMoveCost();
    }

    private ActionType getAction(TrooperStance start, TrooperStance end){
        if (start.equals(end)){
            return null;
        }
        switch (start){
            case KNEELING:
                if (end.equals(TrooperStance.STANDING)){
                    return ActionType.RAISE_STANCE;
                }else{
                    return ActionType.LOWER_STANCE;
                }
            case PRONE:
                return ActionType.RAISE_STANCE;
            case STANDING:
                return ActionType.LOWER_STANCE;
        };
        return null;
    }

    private TrooperStance getBestDMGStance(Trooper t, Game game){
        int standingDmg = t.getStandingDamage();
        int kneelingDmg = t.getKneelingDamage();
        int proneDmg = t.getProneDamage();
        switch (t.getStance()){
            case KNEELING:
                kneelingDmg = t.getActionPoints()/t.getShootCost()*t.getKneelingDamage();
                proneDmg = (t.getActionPoints()-game.getStanceChangeCost())/t.getShootCost()*t.getProneDamage();
                if (proneDmg>kneelingDmg){
                    return TrooperStance.PRONE;
                }else{
                    return TrooperStance.KNEELING;
                }
            case PRONE:
                return TrooperStance.PRONE;
            case STANDING:
                standingDmg = t.getActionPoints()/t.getShootCost()*t.getStandingDamage();
                kneelingDmg = (t.getActionPoints()-game.getStanceChangeCost())/t.getShootCost()*t.getKneelingDamage();
                if (kneelingDmg>standingDmg){
                    return TrooperStance.KNEELING;
                }
                return TrooperStance.STANDING;
        };

        return TrooperStance.PRONE;
    }


    private boolean isKneelingBetter(Trooper t, Game game){
        if (t.getActionPoints()>game.getStanceChangeCost()){
            int stanceDmg = t.getActionPoints()/t.getShootCost()*t.getStandingDamage();
            int kneelingDmg = (t.getActionPoints()-game.getStanceChangeCost())/t.getShootCost()*t.getKneelingDamage();
            if (kneelingDmg>stanceDmg){
                return true;
            }
        }
        return false;
    }

    private boolean isBonusReachable(Bonus bonus, World world) {
        for (Trooper t : world.getTroopers()) {
            if (t.getX() == bonus.getX() && t.getY() == bonus.getY()) {
                return false;
            }
        }
        return true;
    }


    private static int nextWPX = -1;
    private static int nextWPY = -1;
    private static boolean wpReached = false;

    private Direction getCWDirection(Trooper self, World world, Game game, Move move, PathFinder pf) {
        wpReached = nextWPX>=0 && nextWPY>=0 && self.getDistanceTo(nextWPX,nextWPY)<=3;
        if (nextWPX<0 || nextWPY<0 || wpReached){
            if (self.getX() < 15 && self.getY() < 10){
                nextWPX = 29;
                nextWPY = 0;
            }
            else if (self.getX() >= 15 && self.getY() < 10){
                nextWPX = 29;
                nextWPY = 19;
            }
            else if (self.getX() >= 15 && self.getY() >= 10){
                nextWPX = 0;
                nextWPY = 19;
            }
            else if (self.getX() < 15 && self.getY() >= 10){
                nextWPX = 0;
                nextWPY = 0;
            }
            else{
                nextWPX = 15;
                nextWPY = 10;
            }
        }

        Direction ret = Direction.CURRENT_POINT;
        ret = pf.getDirection(self, world, nextWPX, nextWPY, 3);
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


    private int getFriendlyCount(Trooper self, World world, Game game, Move move){
        int ret = 0;
        for (Trooper t : world.getTroopers()){
            if (t.isTeammate()){
                ret++;
            }
        }
        return ret;
    }

    private Trooper getPrimary(Trooper self, World world, Game game, Move move){
        if (primaryId>=0 && primaryType !=null){
            for (Trooper t : world.getTroopers()){
                if (t.getPlayerId()==primaryId && t.getType().equals(primaryType)){
                    return t;
                }
            }
        }
        return null;
    }

    private void medicTactic(Trooper self, World world, Game game, Move move, PathFinder pf) {
        Trooper soldier = getSoldier(self, world, game, move);
        Trooper enemy = getEnemy(self, world, game, move);


        if (getFriendlyCount(self, world, game, move)!=1){
            if (self.getHitpoints() < 50 && self.isHoldingMedikit()) {
                if (self.getActionPoints() >= game.getMedikitUseCost()) {
                    move.setAction(ActionType.USE_MEDIKIT);
                    move.setDirection(Direction.CURRENT_POINT);
                    return;
                }
            }



//
//            if (enemy!=null && enemy.getHitpoints()<(self.getDamage()*(self.getActionPoints()/self.getShootCost()))){
//
//            }


            if (self.getHitpoints() < 50) {
                if (self.getActionPoints() >= game.getFieldMedicHealCost()) {
                    move.setAction(ActionType.HEAL);
                    move.setDirection(Direction.CURRENT_POINT);
                    return;
                }
            }


            boolean saveSoldier = false;


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
        }






        if (null != enemy && enemy.getHitpoints() > 0) {
            if (self.getActionPoints() >= game.getGrenadeThrowCost() && self.isHoldingGrenade()){
                if( self.getDistanceTo(enemy) <= game.getGrenadeThrowRange()) {
                    System.out.println("grenade!" + game.getGrenadeThrowCost());
                    move.setAction(ActionType.THROW_GRENADE);
                    move.setX(enemy.getX());
                    move.setY(enemy.getY());
                    return;
                }else if (self.getActionPoints() >= game.getGrenadeThrowCost()+moveCost(self, game)){
                    System.out.println("come close");
                    move.setAction(ActionType.MOVE);
                    move.setDirection(pf.getDirection(self, world, enemy.getX(), enemy.getY(), game.getGrenadeThrowRange()));
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

        if (self.getHitpoints() < self.getMaximalHitpoints()) {
            if (self.getActionPoints() >= game.getFieldMedicHealCost()) {
                move.setAction(ActionType.HEAL);
                move.setDirection(Direction.CURRENT_POINT);
                return;
            }
        }

        if (null != soldier && soldier.getHitpoints() > 0) {
            if (self.getActionPoints() >= game.getStandingMoveCost()) {
                move.setAction(ActionType.MOVE);
                Direction direction = pf.getDirection(self, world, soldier.getX(), soldier.getY(), 1);
                if (direction.equals(Direction.CURRENT_POINT)){
                    direction = pf.getDirection(self,world,random.nextInt(30),random.nextInt(20),10);
                }
                move.setDirection(direction);
                return;
            }
        }

        if (self.getActionPoints() >= game.getStandingMoveCost()) {
            move.setAction(ActionType.MOVE);
            move.setDirection(getCWDirection(self, world, game, move, pf));
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

    private Trooper getCommander(Trooper self, World world, Game game, Move move) {
        for (Trooper t : world.getTroopers()) {
            if (t.isTeammate() && t.getType().equals(TrooperType.COMMANDER)) {
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
}
