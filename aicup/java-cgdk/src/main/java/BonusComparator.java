import model.Bonus;
import model.Trooper;

import java.util.Comparator;

/**
 * @author : dkim
 *         Date: 12.11.13
 *         Time: 22:39
 */
public class BonusComparator implements Comparator<Bonus>{
    Trooper self;

    public BonusComparator(Trooper self) {
        this.self = self;
    }

    @Override
    public int compare(Bonus o1, Bonus o2) {
        return self.getDistanceTo(o1) < self.getDistanceTo(o2) ? -1 : 1;
    }
}
