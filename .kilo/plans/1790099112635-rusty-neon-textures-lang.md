# Rusty + neon texture revamp, crash guards, NL lang

## Goal

Regenerate every mod texture as rusty industrial plates with cyan/magenta neon accents. Fix crash-worthy GUI/client bugs. Complete `en_us.json`, then add Dutch `nl_nl.json`.

## Decisions (locked)

- Look: keep rust/brown plates; add cyan + magenta neon lines, LEDs, hologram-style screens. Not gunmetal-clean, not white lab.
- Keep 16x16 Minecraft pixel art. Do not hand-paint hundreds of unique PNGs.
- Reuse existing Python generators in `tools/`. Do not rename `textures/gui/rusty_machine.png` (Java already points at it).
- Dutch locale file: `nl_nl.json` (Minecraft Netherlands Dutch).
- Crash work is in-scope. Recipe/multiblock redesign is out of scope.

## Context

- Assets come from `tools/generate_legacy_assets.py`, `generate_pc_production.py`, `generate_pcb_assets.py`, `generate_tin_nickel_assets.py`, `generate_workstation_desk.py`, `generate_rusty_gui.py`.
- Current GUI (`IndustrialGuiStyle` + `rusty_machine.png`) is brown/rust with no neon.
- PC item textures in `generate_pc_production.py` (`item_tex(seed)`) are random colored squares; replace with typed templates.
- Only lang file today: `src/main/resources/assets/tech_revised/lang/en_us.json`.
- Manual chapters in `PcSurvivalManualPages.java` are hardcoded English.

## 1. Shared rusty-neon style

Add `tools/industrial_style.py` used by every generator:

- Palette: rust dark `(28,16,12)`, plate `(86,52,34)`, rust highlight `(148,64,28)`, rivet `(176,138,104)`, cyan neon `(40,220,255)`, magenta neon `(220,40,180)`, amber energy `(255,160,40)`, LED off `(40,80,50)`, LED on cyan.
- Helpers: riveted rusty plate, neon edge line, hazard stripe, LED pair, hologram screen, PCB with cyan traces.
- Active machine fronts: brighter cyan LED + magenta scanline. Idle: dim LED, rust only.

## 2. Regenerate all textures

Run generators after palette/helpers land. Overwrite existing PNGs; keep model/blockstate JSON paths.

| Generator | Change |
| --- | --- |
| `generate_rusty_gui.py` | Same rusty plates + cyan neon frame, magenta corner ticks, amber energy well. Also restyle `crusher_gui.png` and `jei_gui.png`. |
| `generate_legacy_assets.py` | Rusty `machine_side` / `machine_top` / frames. Neon on controllers, buses, hatches. Items keep shape, rust metals + neon on configurator/electrode. |
| `generate_pc_production.py` | Same rusty chassis. Replace `item_tex(seed)` with templates: pcb, chip, cable, pellet, case, glass, speaker. |
| `generate_pcb_assets.py` | FR4/PCB items get cyan traces + magenta pads. Machines use shared rusty chassis. |
| `generate_tin_nickel_assets.py` | Ores/ingots/nuggets/raws in rusty-industrial metals. |
| `generate_workstation_desk.py` | Desk/chair/kit blocks rusty metal + cyan screen glow. |

`IndustrialGuiStyle.java`: keep rust panel colors; set `TITLE_COLOR` cyan-tint (`0xB8E8F0`), `GOOD_COLOR` cyan, `BAD_COLOR` magenta, energy/progress fill amber/cyan. Rivet colors stay rust.

Fluids stay vanilla water overlay + tint (already in `ModFluids`). Do not add new fluid PNG atlases.

## 3. Crash / client bugs

Fix these; do not expand into VM-feature work.

1. **Null/wrong BE on menu open** — `GenericIndustrialMachineMenu`, `MultiInputIndustrialMachineMenu`, `CrusherMenu`, `ElectricArcFurnace*Menu`, `MultiblockControllerMenu` cast `getBlockEntity(...)` with no null check. Guard: if entity is null or wrong type, skip TE slots and keep menu open (or close safely). Never NPE/CCE.
2. **Client setup thread** — wrap `MenuScreens.register` and `ItemBlockRenderTypes.setRenderLayer` in `event.enqueueWork(...)` in `TechRevised.clientSetup`.
3. **Missing fluid layers** — add translucent layers for liquid glass + sodium carbonate / cupric chloride / sodium hydroxide (still + flowing).
4. **Missing bucket tints** — register `NAPHTHA_BUCKET`, `ETHYLENE_BUCKET`, `CHLORINE_BUCKET`, `VINYL_CHLORIDE_BUCKET` in `ModItemColorHandlers` next to the other buckets.

Out of scope: VM screenshot decode (already try/catch + texture release), recipe logic, multiblock layouts.

## 4. Language

### `en_us.json` gaps

Add keys, then wire Java off literals:

- Manual: `screen.tech_revised.manual.chapter.<id>.title` / `.body` for every chapter in `PcSurvivalManualPages`. Stop embedding English in Java.
- JEI literals in category classes: Time, Energy, Any order, Machine, Layers, Creosote, O2, Output, Size. Use `jei.tech_revised.*`.
- Hatch/bus chat: `ElectricArcFurnaceEnergyInputHatchBlock`, `*FluidInputBusBlock`, `*FluidOutputBusBlock` (`Energy:`, `Water:`, `Stored Fluid:`).
- `MultiInputIndustrialMachineBlockEntity.notifyNearby`: `message.tech_revised.process_failed`.
- GUI tooltips: `gui.tech_revised.energy`, `gui.tech_revised.progress_percent`, `gui.tech_revised.fluid_mb`.
- Block-item names missing as item keys (inventory already falls back to block keys; add anyway): `item.tech_revised.tested_desk`, `tested_chair_seat`, `tested_chair_backrest`.
- Remove leftover `config.jade.plugin_pipez.pipe`.

Keep existing item/block/fluid/JEI/message/advancement/jade keys. Do not rename keys.

### Dutch

Create `src/main/resources/assets/tech_revised/lang/nl_nl.json` with **every** key from the completed `en_us.json`. Translate names, JEI, messages, manual chapters, advancements, jade. Keep acronyms (PCB, GPU, HDMI, AOD, FE, mB).

## 5. Validation

- Run all `tools/generate_*.py` from repo root (except `bom_exploder.py`).
- Confirm no missing `layer0` item textures vs `models/item/*.json`.
- Parse both lang JSON files; every `en_us` key exists in `nl_nl`.
- Grep Java for remaining user-facing `Component.literal(` English (JEI/hatch/manual/process failed). Numeric coords in JEI layout hover may stay literal.
- Compile (`gradlew compileJava` / IDEA build). Client setup still registers screens.

## Order of work

1. `tools/industrial_style.py` + generator/GUI palette changes, then regenerate PNGs.
2. Crash/client guards.
3. Finish `en_us.json` + Java i18n.
4. Add `nl_nl.json`.
5. Validate as above.
