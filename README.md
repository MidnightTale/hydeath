Hydeath is an essential plugin designed for Spigot, Paper, and Folia servers. It significantly enhances the gameplay experience by mitigating the frustration of losing items upon death, making the process of recovering lost items seamless and efficient.

### config.yml
```yml
# config.yml

# Spread amount for dropped items
# Adjust how much the dropped items spread out when a player dies
spreadAmount: 0.2

expDropPercent: 70


customDeathMessage: false

# Item settings for dropped items
itemSettings:
  canMobPickup: false   # Allow mobs to pick up the dropped items
  invulnerable: true   # Make dropped items invulnerable
  glowing: true        # Make dropped items glow
  unlimitedLifetime: true  # Make dropped items never despawn
  canOwnerPickupOnly: true # Allow only owner of death item to pickup
  # canHopperPickup: false #in dev
  # canEntityPickup: false #in dev
```
Credit: [Easy Deaths](https://github.com/ringprod/easy-deaths)
