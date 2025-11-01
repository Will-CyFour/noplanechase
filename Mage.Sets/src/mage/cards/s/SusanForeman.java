package mage.cards.s;

import java.util.UUID;
import mage.MageInt;
import mage.abilities.keyword.DoctorsCompanionAbility;
import mage.abilities.mana.GreenManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;

/**
 * @author CyFour
 */
public final class SusanForeman extends CardImpl {

    public SusanForeman(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{1}{G}");
        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.TIME_LORD);

        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        //Planeswalk

        // {T}: Add {G}.
        this.addAbility(new GreenManaAbility());

        // Doctor's companion
        this.addAbility(DoctorsCompanionAbility.getInstance());
    }

    private SusanForeman(final SusanForeman card) {
        super(card);
    }

    @Override
    public SusanForeman copy() {
        return new SusanForeman(this);
    }

}
