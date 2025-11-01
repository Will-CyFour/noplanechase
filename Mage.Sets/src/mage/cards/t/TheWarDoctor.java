
package mage.cards.t;

import mage.MageInt;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.BatchTriggeredAbility;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.AttacksTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.abilities.effects.common.replacement.DiesReplacementEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.events.ZoneChangeBatchEvent;
import mage.game.events.ZoneChangeEvent;
import mage.game.permanent.Permanent;
import mage.target.common.TargetAnyTarget;

import java.util.UUID;

/**
 * 1
 * @author CyFour
 */
public final class TheWarDoctor extends CardImpl {

    public TheWarDoctor(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{2}{R}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.TIME_LORD);
        this.subtype.add(SubType.DOCTOR);

        this.power = new MageInt(3);
        this.toughness = new MageInt(5);

        // Whenever one or more other permanents phase out and whenever one or more other cards are put into exile from anywhere, put a time counter on The War Doctor.
        this.addAbility(new TheWarDoctorTriggeredAbility());

        // Whenever The War Doctor attacks, it deals damage equal to the number of time counters on it to any target. If a creature dealt damage this way would die this turn, exile it instead.
        Ability ability = new AttacksTriggeredAbility(new TheWarDoctorDamageEffect(), false);
        ability.addTarget(new TargetAnyTarget());

        this.addAbility(ability);
    }

    private TheWarDoctor(final TheWarDoctor card) {
        super(card);
    }

    @Override
    public TheWarDoctor copy() {
        return new TheWarDoctor(this);
    }
}

class TheWarDoctorTriggeredAbility extends TriggeredAbilityImpl implements BatchTriggeredAbility<ZoneChangeEvent> {

    public TheWarDoctorTriggeredAbility() {
        super(Zone.BATTLEFIELD, new AddCountersSourceEffect(CounterType.TIME.createInstance()));
        setTriggerPhrase("Whenever one or more other permanents phase out and whenever one or more other cards are put into exile from anywhere, ");
        this.usesStack = false;
    }

    private TheWarDoctorTriggeredAbility(final TheWarDoctorTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public TheWarDoctorTriggeredAbility copy() {
        return new TheWarDoctorTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ZONE_CHANGE_BATCH
                || event.getType() == GameEvent.EventType.PHASED_OUT;
    }

    @Override
    public boolean checkEvent(ZoneChangeEvent event, Game game) {
        if (event.getFromZone() == Zone.BATTLEFIELD && event.getToZone() == Zone.OUTSIDE) {
            return event.getTargetId() != null && !event.getTargetId().equals(getSourceId());
        }
        if (event.getToZone() == Zone.EXILED) {
            return event.getTargetId() != null && !event.getTargetId().equals(getSourceId());
        }
        return false;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (event.getType() == GameEvent.EventType.PHASED_OUT) {
            return event.getTargetId() != null && !event.getTargetId().equals(getSourceId());
        }
        
        ZoneChangeBatchEvent batchEvent = (ZoneChangeBatchEvent) event;
        boolean hasPhaseOut = false;
        boolean hasExile = false;
        
        for (ZoneChangeEvent zEvent : batchEvent.getEvents()) {
            if (zEvent.getTargetId() != null && !zEvent.getTargetId().equals(getSourceId())) {
                if (zEvent.getFromZone() == Zone.BATTLEFIELD && zEvent.getToZone() == Zone.OUTSIDE) {
                    hasPhaseOut = true;
                }
                if (zEvent.getToZone() == Zone.EXILED) {
                    hasExile = true;
                }
            }
        }
        
        int counters = (hasPhaseOut ? 1 : 0) + (hasExile ? 1 : 0);
        if (counters > 0) {
            this.getEffects().clear();
            this.addEffect(new AddCountersSourceEffect(CounterType.TIME.createInstance(counters)));
            return true;
        }
        
        return false;
    }

    @Override
    public String getRule() {
        return "Whenever one or more other permanents phase out and whenever one or more other cards are put into exile from anywhere, put a time counter on {this}.";
    }
}

class TheWarDoctorDamageEffect extends OneShotEffect {

    public TheWarDoctorDamageEffect() {
        super(Outcome.Damage);
        this.staticText = "it deals damage equal to the number of time counters on it to any target. If a creature dealt damage this way would die this turn, exile it instead";
    }

    protected TheWarDoctorDamageEffect(final TheWarDoctorDamageEffect effect) {
        super(effect);
    }

    @Override
    public TheWarDoctorDamageEffect copy() {
        return new TheWarDoctorDamageEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent sourcePermanent = source.getSourcePermanentIfItStillExists(game);
        if (sourcePermanent == null) {
            return false;
        }
        
        int damage = sourcePermanent.getCounters(game).getCount(CounterType.TIME);
        if (damage > 0) {
            UUID targetId = getTargetPointer().getFirst(game, source);
            if (targetId != null) {
                Permanent targetPermanent = game.getPermanent(targetId);
                if (targetPermanent != null) {
                    targetPermanent.damage(damage, sourcePermanent.getId(), source, game);
                    if (targetPermanent.isCreature(game)) {
                        game.addEffect(new DiesReplacementEffect(new MageObjectReference(targetPermanent, game), Duration.EndOfTurn), source);
                    }
                } else {
                    game.getPlayer(targetId).damage(damage, sourcePermanent.getId(), source, game);
                }
                return true;
            }
        }
        return false;
    }
}
