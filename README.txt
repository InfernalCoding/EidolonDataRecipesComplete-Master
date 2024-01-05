Write each recipe in a Datapack/Mod with the folder format: data, "namespace", recipes
Create a .json file for each recipe
Anything written in ()'s isn't a needed part of the code

#Workbench Example
{
  "type": "eidolon:worktable",
  "core": [ (Specify keys with ""'s, followed by a comma to seperate each one)
    " D ",
    " A ",
    " G "
  ],
  "extras": [
    "Z",
    "SS",
    "W"
  ],
  "key": { (Specify an item or tag id for each ingredient; format is "modId:itemID")
    "D": {
      "tag": "forge:storage_blocks/diamond"
    },
    "G": {
      "tag": "forge:glass"
    },
    "A": {
      "item": "eidolon:basic_amulet"
    },
    "Z": {
      "item": "eidolon:zombie_heart"
    },
    "W": {
      "item": "eidolon:wraith_heart"
    },
    "S": {
      "item": "eidolon:lesser_soul_gem"
    }
  },
  "result": {
    "item": "eidolon:glass_hand"
  }
}

#Crucible Example
{
  "type": "eidolon:crucible",
  "result": {
    "item": "eidolon:shadow_gem"
  },
  "steps": [ (A step is the action taken before the crucible bubbles, which can be putting in an item or right-clicking the crucible to stir it)
    { (Step 1)
      "ingredients": [
        {
          "item": "minecraft:coal"
        }
      ]
    },
    { (Step 2)
      "ingredients": [
        {
          "item": "minecraft:ghast_tear"
        },
        {
          "item": "eidolon:death_essence"
        }
      ],
      "stirs": 1
    },
    {
      "ingredients": [
        {
          "item": "eidolon:soul_shard",
          "count": 2
        },
        {
          "item": "eidolon:death_essence"
        }
      ],
      "stirs": 1
    },
    {
      "ingredients": [
        {
          "tag": "forge:gems/diamond"
        }
      ]
    }
  ]
}

#Ritual Example
{
  "type": "eidolon:ritual",
  "symbol": "eidolon:particle/crystal_ritual", (Choose a particle texture id for the ritual)
  "title": "Netherite Block Ritual", (Sets the title of the Ritual in the Eidolon Guidebook)
  "description": "A ritual which turns all gold blocks in the surrounding area into netherite blocks", (Sets the description of the ritual in the Eidolon Guidebook)

  "requirements": { (Sets the required item for each of the ritual's item slots)
    "brazier": { (The center of the ritual where an item is first burned)
      "type": "item", (Sets the item slot to be a tag or item)
      (Same as value of "type") "item": "minecraft:gold_ingot" (Define a tag or item id here)
    },

    "health": 40, (Requires 40 hp from nearby entities for the ritual to succeed)

    "items": [
      {
        "focused": true, (true -> the item slot is a stone hand block; false -> the item slot is a necrotic focus block)
        "type": "item",
        "item": "minecraft:gold_ingot"

        (Extra examples:
            {
                    "focused": true,
                    "type": "item", (Sets a necrotic focus to require an awkward potion)
                    "potion": "minecraft:awkward" (Any valid potion id)
            }

            {
                     "focused": true,
                     "type": "item", (Sets a necrotic focus to require an awkward splash potion)
                     "splash_potion": "minecraft:awkward" (Any valid potion id)
            }

            {
                      "focused": true,
                      "type": "item", (Sets a necrotic focus to require an awkward lingering potion)
                      "lingering_potion": "minecraft:awkward" (Any valid potion id)
            }

            {
                      "focused": true,
                      "type": "item", (Sets a necrotic focus to require a sharpness 2 enchantment book)
                      "enchantment": "minecraft:sharpness" (Any valid enchantment id),
                      "level": 2 (Sets the enchantment level)
            }
         )
      },
      {
        "focused": false,
        "type": "item",
        "item": "minecraft:gold_ingot"
      }
    ]
  },

  "extras": [ (Defines additional requirements for the ritual to succeed)
    {
      "dimension": "minecraft:overworld" (Requires the ritual to take place in the overworld)
      (Extra examples :
        "advancement": "apple:example" (requires the advancement "example" for the ritual to succeed)
        "experience": 10 (Charges 10 experience levels from a nearby player for each stone hand/necrotic focus item slot consumed by the ritual)
    }
  ],

  "results": [ (defines different results to occur after the ritual succeeds; there can be multiple consecutive results so be creative)
    { (types: "absorb" converts nearby mobs of a set type to a specified item, which has a set a count, along with a random add modifier for each of that item spawned
              "allure" lures nearby mobs of the set type to the ritual site
              "deceit" artificially increases the player's reputation with nearby villagers
              "item" spawns a specified item on the ritual center
              "repel" repels nearby mobs of the set type from the ritual site
              "summon" summons in a specified mob
              "time" sets the time to day or night until it's fully day or night
              "transform" turns all nearby mobs of a set type into another set entity type
              "transmute" turns all nearby blocks of a set type into another set block type))
      "type": "transmute",
      "block": "minecraft:gold_block", (The block to transform)
      "newBlock": "minecraft:netherite_block" (The new block)

      (Extra examples:
        {
              "type": "allure",
              "entity": "animal" (This can be "animal", "creature", "monster", "zombie", "villager", "skeleton", "raider", "illager", or
              any valid mob id)
        }

        {
              "type": "absorb",
              "entity": "monster", (Sets the entity to be absorbed)
              "item": "eidolon:soul_shard", (The resulting item)
              "count": 1, (The amount of times that this item is created)
              "addModifier": 3 (An additive max modifier for each time an item is created)
        }

        {
              "type": "time",
              "is_day": true (true -> turn the time to day, false -> turn the time to night)
        }

        {
              "type": "deceit"
        }

        {
              "type": "transform",
              "entityOne": "minecraft:zoglin", (entity to transform)
              "entityTwo": "minecraft:hoglin" (transformed entity),
              "count": 1 (amount of the transformed entity to spawn in the world)
        }

        {
              "type": "repel",
              "entity": "monster"
        }

        {
              "type": "item",
              "item": "eidolon:sanguine_amulet"
        }

        {
              "type": "summon",
              "entity": "minecraft:drowned"
        })


    }
  ]

}

#Spell Example
{
  "type": "eidolon:spell",
  "title": "eidolon.codex.page.villager_sacrifice.title", (Title of the Spell in the Eidolon Guidebook)
  "chant": "eidolon.codex.page.villager_sacrifice", (Description of the Spell in the Eidolon Guidebook)
  "deity": "eidolon:dark", (Sets the deity of a spell, only "dark" is available)
  "signs": [ (Sets the signs of the spell; "wicked", "sacred", "blood", "soul", "mind", "warding", and "energy" are available signs)
    "eidolon:blood",
    "eidolon:wicked",
    "eidolon:blood",
    "eidolon:soul"
  ],
  "spell": {
    "type": "eidolon:basic" (set's the spell type; can be either "basic", or "transmute" which turns items into different ones as follows:
    "transmutations": [
          {
            "ingredients": [
              {
                "item": "eidolon:pewter_inlay"
              }
            ],
            "results": [
              {
                "item": "eidolon:unholy_symbol"
              }
            ]
          },
          {
            "ingredients": [
              {
                "item": "minecraft:black_wool"
              }
            ],
            "results": [
              {
                "item": "eidolon:top_hat"
              }
            ]
          },
          {
            "ingredients": [
              {
                "tag": "minecraft:music_discs"
              }
            ],
            "results": [
              {
                "item": "eidolon:music_disc_parousia"
              }
            ]
          }
        ])
  },
  "requirements": [
    {
      "type": "eidolon:reputation",
      "reputation": 15.0 (Reputation required for the Spell's deity)
    },
    {
      "type": "eidolon:altar",
      "effigy": "eidolon:unholy_effigy",
      "altar": "eidolon:stone_altar" (Type of altar required is either "stone_altar" or "wooden_altar")
    },
    {
      "type": "eidolon:goblet", (Indicates that a sacrifice is required for the spell)
      "sacrifice": "eidolon:is_villager_or_player" (can be "any", "is_animal", "is_villager_or_player"; if it's an "entity_type", it'll look like this :
                                                    "sacrifice": "eidolon:entity_type",
                                                    "entity_type", "minecraft:chicken" (Any minecraft mob id))
    }
  ],
  "results": [
    {
      "type": "eidolon:pray" (Prays to a deity in the mod)
    },
    {
      "type": "eidolon:empty_goblet" (Removes blood from the goblet)
    },
    {
      "type": "eidolon:unlock", (Unlocks signs in the Eidolon Guidebook)
      "lock": "eidolon:sacrifice_villager" (Sets what sign lock to unlock; "sacrifice_mob" and "sacrifice_villager" are available locks)
    },
    {
      "type": "eidolon:reputation", (Increases the player's reputation which unlocks signs/spells;
                                     a reputation of 3 is required to unlock the "blood" sign and the "sacrifice_mob" spell,
                                     a reputation of 15 is required to unlock the "sacrifice_villager" spell and "villager_sacrifice" fact
      "constant": 3, (The base reputation to be added to the player for the specified deity)
      "altar": 0.5 (A multiplier to be supplied to the altar's power which is added with "constant" to the player's current reputation)
     }
  ],
  "particleColor": [255, 230, 138, 226] (r, g, b, alpha)
}
