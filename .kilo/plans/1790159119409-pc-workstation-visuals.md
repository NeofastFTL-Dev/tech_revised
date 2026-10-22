# PC workstation multiblock — layout + IE visuals

## Goal

Make the PC workstation look like one assembled desk, not a pile of mismatched cubes. Match Immersive Engineering / this mod’s formed machines (`coke_oven_frame_formed`, `steel_frame` CTM, riveted `machine_top`).

Keep block IDs, recipes, and transport-frame legs. Do not touch PC production machines.

## Locked decisions

- Fix layout **and** visuals (user confirmed).
- Swap the `z = -1` and `z = +1` rows so the structure matches the manual in `en_us.json` / `nl_nl.json`.
- Tower follows the keyboard. Power cable stays at `(1, -1, 2)` as a floor strip behind the desk.
- Shared 4px tabletop on every desk-surface block. IE factory palette. CTM on the desk top.
- No new blocks.

## Why it looks wrong today

Local `+Z` is behind the controller. Code puts keyboard/mouse/HDMI at `z = +1` (behind) and speakers/extra desk at `z = -1` (in front of the screen). Manual already describes the opposite.

`tested_desk` is a 12px slab from y=4–16. Monitor/keyboard/mouse/speakers/HDMI use a 4px slab at y=0–4. Surfaces do not meet.

Textures are generated with leftover cyan/magenta UV markers. Devices reuse `pcb_machine_top`. Monitor screen is a 1px-thin quad. Tower is a floating brick. `WorkstationKitBlock` has no `noOcclusion` and no voxel shapes.

## Target layout

Controller: tested monitor at `(0,0,0)`, facing the chair.

```
y=0  back  (z=+1): speaker | desk | speaker
     mid   (z= 0): desk    | MON  | desk
     front (z=-1): mouse   | kbd  | HDMI

y=-1 back  (z=+1): frame | frame | frame
     mid   (z= 0): frame |       | frame
     front (z=-1): frame | tower | frame
     floor (z=+2):             power @ x=1

y=0/1 chair (z=-2): seat, backrest on top
```

Mechanical change: swap every current `z = -1` part with `z = +1` in:

- `WorkstationStructure.java`
- `MachineLayouts.java` workstation builder
- `MultiblockStructureBlueprint.workstation()`

Power stays `(1, -1, 2)`. Update comments only; lang/manual already match the new layout.

## Models

Update `tools/generate_workstation_desk.py` and regenerate (or edit the JSON it emits) under `assets/tech_revised/models/block/`.

Shared desk slab on **all** y=0 surface blocks (monitor, desk, keyboard, mouse, HDMI, speakers):

- Tabletop `[0, 12, 0] → [16, 16, 16]`
- Thin steel apron on the player-facing edge only where it reads as a desk front (keyboard / mouse / HDMI)

Per-block extras, north = screen/player side:

| Block | Shape |
|---|---|
| `tested_desk` | Tabletop + apron only. No more y=4–16 brick. |
| `tested_monitor` | Stand on the tabletop, 2–3px-thick 14×10 screen facing north, bezel. Keep `_on` variant. |
| `tested_keyboard` | Full-width key deck on the tabletop, 2px tall. |
| `tested_mouse` | Small body + wheel on the right half of the slab (mousepad). |
| `tested_speaker` | Vertical cabinet on the tabletop, driver facing north. |
| `tested_hdmi_cable` | Cable run on the tabletop toward the monitor, not a random ribbon. |
| `tested_desktop_tower` | Upright ATX case filling most of the under-desk cell, front panel north. |
| `tested_power_cable` | Low floor power strip + cable stub (sits behind the desk). |
| `tested_chair_seat` | Pedestal + cushion. |
| `tested_chair_backrest` | Tall back on the far side from the desk so it faces the monitor after facing-rotation. |

`ambientocclusion: false` not required. Do set `"gui_light": "front"` on item parents if the block models look dark in inventory.

## Textures (IE language)

Replace generated workstation PNGs. Drop cyan/magenta debug pixels.

Palette (copy rivets/panels from existing factory art, do not reuse `pcb_machine_top` as a shell):

- Desk top: industrial wood or brushed metal, plank/panel grooves, **no** UV debug lines
- Desk side / apron: dark steel + bolts, same language as `steel_frame.png` / `coke_oven_frame_formed.png`
- Device shell: dark plastic/steel, not PCB beige
- Unique 16×16 faces: keyboard (real key rows), monitor (large screen + bezel + power LED), tower (drive bays, power button, vent), speakers (cone + tweeter), mouse, HDMI connector, chair fabric

Add CTM like `steel_frame.png.mcmeta`:

- `tested_desk_top.png.mcmeta` → `type: ctm`, extra sheet `tested_desk_top_ctm.png` (12-tile CTM layout, same as `steel_frame_ctm.png`)
- Apply the CTM texture on **up** faces of desk-surface models so the 3×2 top reads as one slab

Keep `tested_monitor_front_on.png` as a lit Windows-like screen.

## Block behavior

`pcbMachineProperties()` is full-cube iron. Custom models will x-ray without this:

1. `WorkstationKitBlock` and `WorkstationMonitorBlock`: `.noOcclusion()`.
2. `WorkstationKitBlock`: constructor `VoxelShape` (or small shape enum). Implement `getShape` rotated by `FACING` (copy the facing-rotate pattern from `SolidFuelGeneratorBlock`).
3. Suggested shapes (unrotated, north-facing):
   - desk / keyboard / mouse / hdmi / speaker: tabletop slab y=12–16 plus the extra bits
   - tower: tall case
   - power: low strip
   - chair seat / backrest: match models
4. Monitor: same tabletop + screen shape; also `.noOcclusion()`.
5. `isPathfindable` false is fine as default.

Do not give `transport_frame` a special workstation model (used by every factory machine).

## Files to touch

- `src/main/java/com/neofast/tech_revised/multiblock/WorkstationStructure.java`
- `src/main/java/com/neofast/tech_revised/multiblock/MachineLayouts.java`
- `src/main/java/com/neofast/tech_revised/integration/jei/MultiblockStructureBlueprint.java`
- `src/main/java/com/neofast/tech_revised/block/custom/WorkstationKitBlock.java`
- `src/main/java/com/neofast/tech_revised/block/custom/WorkstationMonitorBlock.java`
- `src/main/java/com/neofast/tech_revised/block/ModBlocks.java` (noOcclusion + shape args only)
- `tools/generate_workstation_desk.py`
- `src/main/resources/assets/tech_revised/models/block/tested_*.json`
- `src/main/resources/assets/tech_revised/textures/block/tested_*.png` (+ desk-top CTM + mcmeta)

Out of scope: recipes, loot, blockstates facing keys, PC production machines, transport_frame texture.

## Validation

- Formed hologram / JEI blueprint: keyboard between chair and monitor, speakers behind, tower under keyboard.
- Adjacent desk tops are one continuous y=16 plane; no 12px brick next to a 4px slab.
- No cyan/magenta pixels.
- Custom models do not show interior x-ray (noOcclusion).
- Existing workstation still crafts; only placement order of the swapped row changes.
- `nl_nl.json` manual already matches; no lang rewrite unless a comment-only string is wrong.
