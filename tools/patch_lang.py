"""Merge i18n keys into en_us.json and write nl_nl.json. Run from repo root."""
import json
import os
import re

LANG_DIR = os.path.join("src", "main", "resources", "assets", "tech_revised", "lang")
EN = os.path.join(LANG_DIR, "en_us.json")
NL = os.path.join(LANG_DIR, "nl_nl.json")

EXTRA = {
    "gui.tech_revised.energy": "%s / %s FE",
    "gui.tech_revised.progress_percent": "%s%%",
    "gui.tech_revised.fluid_mb": "%s / %s mB",
    "gui.tech_revised.fluid_empty": "Empty",
    "gui.tech_revised.stored_fluid": "Stored Fluid: %s (%s / %s mB)",
    "message.tech_revised.process_failed": "Process failed: %s",
    "jei.tech_revised.time": "Time: %s t",
    "jei.tech_revised.energy_rate": "Energy: %s FE/t (%s FE)",
    "jei.tech_revised.energy_total": "Energy: %s FE",
    "jei.tech_revised.any_order": "Any order",
    "jei.tech_revised.machine": "Machine",
    "jei.tech_revised.layers": "Layers (top -> bottom)",
    "jei.tech_revised.creosote_mb": "Creosote: %s mB",
    "jei.tech_revised.oxygen_mb": "O2: %s mB",
    "jei.tech_revised.output_mb": "Output: %s mB",
    "jei.tech_revised.size": "Size: %sW x %sL x %sH",
    "jei.tech_revised.ticks_energy": "%st / %s FE",
    "jei.tech_revised.ticks_energy_rate": "%st / %s FE/t (%s FE)",
    "item.tech_revised.tested_desk": "Workstation Desk",
    "item.tech_revised.tested_chair_seat": "Office Chair Seat",
    "item.tech_revised.tested_chair_backrest": "Office Chair Backrest",
    "screen.tech_revised.manual.click_chapter": "Click a chapter.",
}

CHAPTERS = {
    "the_goal": ("The Goal", """Build a complete PC in survival, then assemble the workstation.

You need:
- 1 Tested Desktop Tower
- 1 Tested Monitor (the controller)
- 1 Tested Keyboard
- 1 Tested Mouse
- 1 Tested HDMI Cable
- 1 Tested Power Cable
- 2 Tested Speakers
- 3 Workstation Desks
- Office Chair Seat + Backrest
- Transport Frames for the desk legs

JEI shows exact recipes. This book is the order of operations.
Every production machine must be a formed multiblock and fed Forge Energy."""),
    "factory_rules": ("Factory Rules", """1. Craft the machine block, place it facing you.
2. Form its multiblock from Transport Frames plus an Energy Input Hatch.
3. Shift-right-click the controller for a hologram (cyan missing, green ok, red wrong).
4. Right-click with a Configurator to auto-place frames/hatches from your inventory.
5. Power it: see the Solid Fuel Generator chapter.
6. Open the GUI only after the structure is valid.
7. Multi-input machines take several items at once. Single-input machines take one.

Production machines use Transport Frames in IRL class silhouettes. Heavy industry (EAF, coke, drill, AOD) uses its own frames."""),
    "configurator": ("Configurator", """Craft: iron + redstone + comparator.

Use:
- Right-click any machine controller to auto-build the structure from items in your inventory.
- Shift-right-click to project the hologram without building.
- JEI has a Multiblock Layout category (Configurator icon) for every structure.

Keep stacks of Transport Frames and Energy Input Hatches on you. You will build dozens of machines."""),
    "first_tools": ("First Tools", """Early crafts (vanilla table):
- Crusher: iron, furnace, piston, redstone
- Solid Fuel Generator: iron, furnace, redstone
- Configurator: iron, redstone, comparator
- This manual: book + redstone
- Electrode: 4 Graphite Dust (crush graphite ore, or 2 coal + flint)
- Transport Frame and Energy Input Hatch: steel-era, after your first ingots

Get iron, copper, coal, redstone, sand, clay, kelp/slime, gold, quartz, and an ink sac. You will burn through all of them."""),
    "ore_crushing": ("Ore & Crushing", """Mine:
- Graphite Ore -> Crusher -> 2 Graphite Dust (or 2 coal + flint)
- Chromite Ore -> Crusher -> 2 Chromite Dust
- Tin Ore (common, near surface) -> 2 Raw Tin
- Nickel Ore (deep) -> 2 Raw Nickel
- Vanilla iron, copper, coal, redstone, gold, quartz, emerald

Raw ores smelt or blast straight to ingots.
Crush raw tin/nickel for dust when a recipe needs it.
Electrode: craft from 4 Graphite Dust. One electrode
runs 256 EAF operations; the blast furnace needs none.

Chromite Dust smelts to Chromite Ingot.
Craft Chromium Dust from chromite.
Craft Carbon Chunk from coal or charcoal.
Craft Silica Sand (sand+flint), Limestone Dust, Dolomite Dust, Soda Ash, Clay Dust, Chemical Lubricant.

Build the Crusher multiblock (hopper mill of Transport Frames plus an Energy Input Hatch) and power it."""),
    "mining_budget": ("Mining Budget", """Rough totals to build the ENTIRE factory once:
every machine, every structure, one full PC + desk.

- ~2,300 Iron Ore
- ~420 Copper Ore
- ~420 Coal (coke, carbon, steel)
- ~600 Redstone
- ~150 Gold Ore
- ~7 Chromite Ore, ~10 Graphite Ore
- ~6 Tin Ore, ~1 Nickel Ore (deep)
- 3 Diamonds, 1 Emerald, 2 Quartz
- 50 Buckets crude oil (drilled, not mined)

The first PC pays for the whole factory; later copies
of the tower and peripherals cost only their own parts."""),
    "solid_fuel_generator": ("Solid Fuel Generator", """The standalone power source. Right-click it with any
furnace fuel (coal, charcoal, coal coke) to load one item.

Burn time converts to FE at 120 FE/tick, stored in a
60,000 FE buffer. It auto-pushes to adjacent Energy
Input Hatches and machines, and cable mods can drain
it directly. Coal coke burns roughly twice as long
as coal - use it once the Coke Oven is running.

One coal = 1,600 FE. Machines run 20-80 FE/tick, so
budget a few stacks per machine-line day.

Shift-right-click for a buffer status readout."""),
    "coke_oven": ("Coke Oven", """Craft Coke Oven Controller + Coke Oven Frames.
Structure also needs an Input Bus, Output Bus, and Fluid Output Bus.

Recipe:
Coal (or charcoal) -> Coal Coke + Creosote

Coke is fuel/reductant for steel and stainless.
Creosote is a byproduct fluid; bucket it if you need it later.

Shift-click the controller for the hologram. This is your first large multiblock."""),
    "steel": ("Steel", """Blast Furnace (start here, no electrode or water):
Iron Ingot + Coal Coke -> 2 Steel Ingots
Iron Ingot + Chromite Dust -> Ferrochromium Ingot
Iron Ingot + Chromium Dust -> Stainless Steel Blend

Structure: EAF frames (3x3 base, six rings, capped top),
Input Bus on the front, Output Bus right, Energy Hatch left.

Electric Arc Furnace:
Iron Ingot + Coal Coke + 250 mB water -> Steel Ingot
Iron Ingot + Chromite Ingot + 250 mB water -> Ferrochromium Ingot

Structure: Transport Frames in a cylinder, plus Input Bus, Output Bus, Fluid Input, Fluid Output, Energy Hatch, and Heater on top. Use the hologram.

You need lots of steel: frames, hatches, sheets, cases, coolers, PSU shells.

Craft Steel Sheets in the Steel Sheet Roller (steel ingot in).
Compress ingots <-> blocks/nuggets at the crafting table."""),
    "stainless": ("Stainless", """Stainless is chromium steel: you need the chromite line.

Blast Furnace (budget):
Ferrochromium Ingot + Nickel Dust -> Stainless Steel Blend,
then smelt/blast the blend into Stainless Steel Ingot.

AOD Converter (proper):
Ferrochromium Ingot + Nickel Dust + 500 mB oxygen
-> Stainless Steel Ingot directly.

Ferrochromium: blast furnace (iron + chromite dust)
or EAF (iron + chromite ingot). Nickel ore spawns deep.

Oxygen comes from the Oxygen Converter multiblock (needs energy + fluid buses).

Stainless nuggets are used on CPU packages and lids. Make a stack."""),
    "crude_oil": ("Crude Oil", """Drilling Platform multiblock: controller, drill head, frames, energy hatch, fluid output bus.

It produces Heavy Crude Oil. Bucket it.

Crude Fractionator:
Heavy Crude Oil Bucket -> Naphtha Bucket

Naphtha is the backbone of plastics, rubber, ABS, and PVC. Build this before any polymer machines."""),
    "plastics": ("Plastics", """Line (each is its own powered multiblock):

Naphtha Steam Cracker: Naphtha -> Ethylene
Chlor-Alkali Cell: Water Bucket + Soda Ash -> Chlorine
VCM Reactor: Ethylene + Chlorine -> Vinyl Chloride
Plasticizer Unit: Naphtha -> Plasticizer
PVC Polymerizer: Vinyl Chloride + Plasticizer -> PVC Pellets
Rubber Compounder: Naphtha -> Rubber Sheet
ABS Polymerizer: Naphtha + Rubber Sheet -> ABS Pellets

PVC jackets cables. ABS molds cases, bezels, keycaps, mouse shells, GPU shrouds, speaker cabinets. Rubber is membranes, wheels, gaskets."""),
    "copper_wire": ("Copper Wire", """Copper Wire Drawer: Copper Ingot -> 4 Copper Wire
Wire Tinning Bath: Copper Wire + Tin Plating Dust -> Tinned Copper Wire

Craft Copper Foil: 2 copper ingots -> 4 foil.
Craft Tin Plating Dust: 2 Raw Tin + redstone -> 4.

You need tinned wire for transformers, spindle motors, voice coils, power conductors, and IC wire-bonding.
Tin nuggets also make Solder Paste (they replaced iron)."""),
    "fiberglass": ("Fiberglass", """This makes FR4 for every circuit board.

1. Batching Mixer: Silica Sand + Limestone Dust + Dolomite Dust + Soda Ash + Clay Dust -> Raw Glass Batch
2. Refractory Melting Furnace: Raw Glass Batch -> Liquid Glass
3. Extrusion (needs Platinum Alloy Bushing Plate machine): Liquid Glass -> Glass Filament
4. Chemical Sizing Applicator: Filament + water -> Sized Glass Filament
5. Gathering Winder: Sized Filament -> Glass Spool
6. Drying Oven: Spool -> Dried Glass Spool
7. Roving Creel: 4 Dried Glass Spools -> Glass Roving
8. Textile Loom: 2 Glass Roving -> Fiberglass Fabric

Craft Raw FR4 Laminate: Fiberglass Fabric + Chemical Lubricant + Slime Ball.

Budget route: 4 Chopped Strand Mat -> 1 Raw FR4 Laminate
(mat is a Strand Chopping Machinery byproduct; skips the loom)."""),
    "bare_pcbs": ("Bare PCBs", """Craft CAD Layout (2 paper + redstone + ink sac).
Design Engineering Station: CAD Layout -> Verified Gerber File.

Then the board line, in order:
1. Industrial Shear: Raw FR4 -> Sheared FR4
2. Decontamination Oven: Sheared -> Baked FR4
3. Hydraulic Cladding Press: Baked FR4 + Copper Foil -> Copper Clad Laminate
4. Photo Resist Applicator: Clad + Photo Resist Polymer -> Inner Layer Patterned Board
5. UV LDI Imager: patterned board + Verified Gerber
6. Chemical Developing Wash -> Developed Inner Layer
7. Acidic Etching Sprayer -> Etched Inner Layer
8. Alkaline Stripping Station -> Stripped Inner Layer
9. Craft Multi-Layer Stackup / Prepreg, then Vacuum Lamination Press -> Laminated PCB Core
10. X-ray Alignment Drill -> Drilled PCB
11. Electroless Plating Bath -> Electroless Copper Coated PCB"""),
    "bare_pcbs_2": ("Bare PCBs 2", """Continue:

The precision machines each need 1 Platinum Alloy Bushing
Plate (gold + steel): X-ray Alignment Drill, Pick and
Place Robot, CNC Router.

12. Outer photoresist + LDI
13. Copper Electroplating Tank -> Electroplated PCB
14. Tin Plating Tank (needs Tin Plating Dust) -> Tin Plated PCB
15. Final etch -> Final Etched PCB
16. Tin strip -> Tin Stripped PCB
17. Soldermask Flooder or UV Pad Exposure + Soldermask Resin -> Masked PCB
18. Inkjet Silkscreen Printer -> Silkscreened PCB
19. Surface Finish Station -> Finished PCB Panel
20. Flying Probe Tester -> Tested PCB Panel
21. CNC Router -> Routed PCB

SMT:
Craft Solder Paste (iron nuggets + redstone + slime).
22. Solder Paste Printer: Routed PCB + Solder Paste -> Stencil Printed PCB
23. Pick and Place: Stencil Printed + Redstone (-> Placed PCBA)
24. Multi-zone Reflow Oven -> Reflowed PCBA
25. AOI Verification Station -> Final PCBA

Final PCBA is the blank used by motherboard, GPU, PSU, SSD, keyboard, mouse, and monitor boards."""),
    "silicon_ics": ("Silicon ICs", """Wafer Blank Press: Silica Sand -> Silicon Wafer
Wafer Dicing Saw: Wafer -> Silicon Die
Die Attach Bonder: Die + Copper Foil -> Attached Die
Wire Bonding Machine: Attached Die + Tinned Copper Wire -> Bonded Die
IC Transfer Mold: Bonded Die + ABS Pellets -> Molded IC
IC Tester: Molded IC -> Tested IC

Every chip in the PC starts as a Tested IC. Make many; binning consumes them one-for-one."""),
    "ic_packages": ("IC Packages", """IC Binning Station recipes (Tested IC + extra):

- Stainless Steel Nugget -> CPU Package
- Redstone -> GPU Package
- Copper Foil -> Chipset Package
- Carbon Chunk -> NAND Package
- Steel Nugget -> HDD Controller Package
- Glass -> Display Controller Package
- Gold Nugget -> MCU Package
- Quartz -> DRAM Package (or craft DRAM from gold/redstone/quartz)

Craft SPD EEPROM: gold nuggets + redstone + emerald.

You need at least: 1 CPU, 1 GPU, 1 chipset, 1 display controller, 1 MCU (keyboard) + 1 MCU (mouse) + 1 MCU (SSD), NAND, HDD controller, and DRAM for RAM and VRAM."""),
    "ram": ("RAM", """Two DRAM sources: craft (gold/redstone/quartz) or bin Tested ICs with quartz.

Pick and Place Robot:
Final PCBA + DRAM Package + SPD EEPROM -> Unprogrammed RAM Module

Flying Probe Tester:
Unprogrammed RAM Module -> RAM Stick

You need a RAM Stick for desktop assembly (DIMM Install Press)."""),
    "motherboard": ("Motherboard", """CPU Socket Press: Final PCBA + Steel Nugget -> Socketed Motherboard
Expansion Slot Inserter: Socketed Motherboard + Copper Foil -> Slotted Motherboard
Chipset Placer: Slotted Motherboard + Chipset Package -> Motherboard PCBA
Motherboard ICT: Motherboard PCBA -> Tested Motherboard

This Tested Motherboard is the start of tower assembly."""),
    "cpu": ("CPU", """IHS Bonder: CPU Package + Stainless Steel Nugget -> Lidded CPU
CPU Burn-In Chamber: Lidded CPU -> Tested CPU

Later, Thermal Paste Dispenser mounts it:
Tested Motherboard + Tested CPU + Chemical Lubricant -> Board With CPU"""),
    "gpu": ("GPU", """GPU Substrate SMT: Final PCBA + GPU Package -> GPU Core Board
VRAM Populator: GPU Core Board + DRAM Package -> GPU Populated Board
GPU Cooler Press: Steel Ingot + Rubber Sheet -> GPU Cooler
GPU Shroud Molder: ABS Pellets -> GPU Shroud
GPU Assembly Jig: Populated Board + Cooler + Shroud -> Assembled GPU
GPU Tester: Assembled GPU -> Tested GPU"""),
    "psu": ("PSU", """Transformer Winder: Tinned Copper Wire + Steel Ingot -> PSU Transformer
PSU SMT: Final PCBA + PSU Transformer -> PSU Board
PSU Case Stamper: Steel Ingot -> PSU Shell
PSU Final Assembly: PSU Board + PSU Shell + PVC Pellets -> Assembled PSU
PSU Load Tester: Assembled PSU -> Tested PSU"""),
    "hdd": ("HDD", """Platter Polisher: Iron Ingot -> HDD Platter
Magnetic Sputter: Platter + Redstone -> Coated Platter
Spindle Motor Winder: Tinned Copper Wire + Steel Nugget -> Spindle Motor
Head Stack Assembler: Steel Nugget + Silicon Die -> Head Stack
HDD HEPA Sealer: Coated Platter + Spindle Motor + Head Stack -> Sealed HDD
HDD Servo Writer: Sealed HDD + HDD Controller Package -> Programmed HDD
HDD Burn-In: Programmed HDD -> Tested HDD

Either a Tested HDD or Tested SSD can be installed; both recipes output Board With Storage."""),
    "ssd": ("SSD", """NAND Stacker: NAND Package -> NAND Stack
SSD SMT: Final PCBA + NAND Stack + MCU Package -> SSD Board
SSD Firmware Programmer: SSD Board -> Programmed SSD
SSD Case Welder: Programmed SSD + ABS Pellets -> Assembled SSD
SSD Validator: Assembled SSD -> Tested SSD"""),
    "case": ("Case", """Steel Sheet Roller: Steel Ingot -> Steel Sheet
Case Stamping Press: Steel Sheet -> Case Chassis
Powder Coater: Case Chassis -> Coated Chassis
Front Panel Molder: ABS Pellets -> Case Front Panel
Case Assembly Jig: Coated Chassis + Front Panel + Rubber Sheet -> PC Case"""),
    "tower_assembly": ("Tower Assembly", """Install in this exact order:

1. Thermal Paste Dispenser: Tested Motherboard + Tested CPU + Chemical Lubricant -> Board With CPU
2. DIMM Install Press: Board With CPU + RAM Stick -> Board With Memory
3. GPU Install Bench: Board With Memory + Tested GPU -> Board With GPU
4. Storage Install Bay: Board With GPU + Tested HDD or Tested SSD -> Board With Storage
5. PSU Install Bench: Board With Storage + Tested PSU -> Wired Internals
6. Tower Final Assembly: Wired Internals + PC Case -> Assembled Desktop
7. Desktop Power-On Test: Assembled Desktop -> Tested Desktop Tower

The tower is a placeable block. Keep it for the workstation."""),
    "monitor": ("Monitor", """LCD Glass Washer: Glass -> Cleaned LCD Glass
Color Filter Printer: Cleaned Glass -> Color Filter Glass
Liquid Crystal Filler: Color Filter Glass + Chemical Lubricant -> Filled LCD Cell
Polarizer Laminator: Filled Cell -> Polarized Panel
Backlight Assembler: ABS Pellets + Redstone -> LED Backlight
LCD Cell Bonder: Polarized Panel + LED Backlight -> LCD Module
Monitor Controller SMT: Final PCBA + Display Controller Package -> Monitor Logic Board
Monitor Bezel Molder: ABS Pellets -> Monitor Bezel
Monitor Final Assembly: LCD Module + Logic Board + Bezel -> Assembled Monitor
Monitor Burn-In: Assembled Monitor -> Tested Monitor

The Tested Monitor is the workstation controller. Face it toward the chair when placing."""),
    "keyboard": ("Keyboard", """Keycap Injection Molder: ABS Pellets -> Keycaps
Membrane Sheet Printer: Rubber Sheet -> Key Membrane
Keyboard PCB SMT: Final PCBA + MCU Package -> Keyboard PCB
Keyboard Plate Stamper: Steel Sheet -> Keyboard Plate
Keyboard Assembly: Keyboard PCB + Key Membrane + Keycaps -> Assembled Keyboard
Keyboard Tester: Assembled Keyboard + Keyboard Plate -> Tested Keyboard"""),
    "mouse": ("Mouse", """Mouse Shell Molder: ABS Pellets -> Mouse Shell
Optical Sensor SMT: Final PCBA + MCU Package -> Mouse Sensor Board
Scroll Wheel Molder: Rubber Sheet -> Scroll Wheel
Mouse Assembly: Shell + Sensor Board + Scroll Wheel -> Assembled Mouse
Mouse Tester: Assembled Mouse -> Tested Mouse"""),
    "hdmi_cable": ("HDMI Cable", """Pair Twinner: Tinned Copper Wire -> Twisted Pair
HDMI Shield Braider: Twisted Pair + Copper Wire -> Shielded HDMI Core
HDMI Jacket Extruder: Shielded Core + PVC Pellets -> Jacketed HDMI
HDMI Connector Molder: ABS Pellets + Gold Nugget -> HDMI Connector
HDMI Termination Press: Jacketed HDMI + Connector -> Assembled HDMI
HDMI Continuity Tester: Assembled HDMI -> Tested HDMI Cable"""),
    "power_cable": ("Power Cable", """Power Conductor Strander: Tinned Copper Wire -> Power Conductor
Power Jacket Extruder: Power Conductor + PVC Pellets -> Jacketed Power Cord
IEC Connector Molder: ABS Pellets + Steel Nugget -> IEC Connector
Power Cable Overmolder: Jacketed Cord + IEC Connector -> Assembled Power Cable
Hi-Pot Tester: Assembled Power Cable -> Tested Power Cable"""),
    "speakers": ("Speakers", """You need TWO tested speakers.

Speaker Magnet Sinter: Iron Ingot + Redstone -> Speaker Magnet
Voice Coil Winder: Tinned Copper Wire -> Voice Coil
Cone Former: Paper + Chemical Lubricant -> Speaker Cone
Driver Assembler: Magnet + Voice Coil + Cone -> Speaker Driver
Cabinet Molder: ABS Pellets -> Speaker Cabinet
Speaker Final Assembly: Driver + Cabinet + Rubber Sheet -> Assembled Speaker
Speaker Anechoic Test: Assembled Speaker -> Tested Speaker"""),
    "desk_chair": ("Desk & Chair", """Craft at the table:
- Workstation Desk: steel sheets + planks (make 3)
- Office Chair Seat: steel + wool
- Office Chair Backrest: steel + wool

Transport Frames are the desk legs. Craft extras from steel."""),
    "workstation": ("Workstation", """Place the Tested Monitor facing the chair. It is the controller.

Relative to the monitor (look at JEI hologram / shift-click):

Desk surface:
- Desks on left, right, and behind the monitor
- Speakers on the back-left and back-right corners
- Keyboard in front of the monitor
- Mouse on the front-left
- HDMI on the front-right

Under the desk:
- Transport Frame legs
- Tested Desktop Tower under the keyboard
- Tested Power Cable one block further under the HDMI side

Chair:
- Seat two blocks in front of the monitor
- Backrest on top of the seat

Right-click the monitor with a Configurator, or shift-click for the hologram. When valid you get "Workstation structure is valid" and can open the VM screen."""),
}

GLOSSARY = [
    ("Workstation Desk", "Werkplekbureau"),
    ("Office Chair Seat", "Bureaustoelzitting"),
    ("Office Chair Backrest", "Bureaustoelleuning"),
    ("Electric Arc Furnace", "Elektrische vlamboogoven"),
    ("Argon Oxygen Decarburization Converter", "AOD-converter"),
    ("Drilling Platform", "Boorplatform"),
    ("Coke Oven", "Cokesoven"),
    ("Oxygen Converter", "Zuurstofconverter"),
    ("Blast Furnace", "Hoogoven"),
    ("Solid Fuel Generator", "Vastebrandstofgenerator"),
    ("Transport Frame", "Transportframe"),
    ("Energy Input Hatch", "Energie-invoerluik"),
    ("Input Bus", "Invoerbus"),
    ("Output Bus", "Uitvoerbus"),
    ("Fluid Input Bus", "Vloeistofinvoerbus"),
    ("Fluid Output Bus", "Vloeistofuitvoerbus"),
    ("Deepslate", "Diepleisteen"),
    ("Stainless Steel", "Roestvast staal"),
    ("Stainless", "Roestvast"),
    ("Heavy Crude Oil", "Zware ruwe olie"),
    ("Liquid Glass", "Vloeibaar glas"),
    ("Cupric Chloride Solution", "Koperchlorideoplossing"),
    ("Sodium Carbonate Solution", "Natriumcarbonaatoplossing"),
    ("Sodium Hydroxide Solution", "Natriumhydroxideoplossing"),
    ("Vinyl Chloride", "Vinylchloride"),
    ("Coal Coke", "Steenkoolcokes"),
    ("Graphite Dust", "Grafietstof"),
    ("Chromite Dust", "Chromietstof"),
    ("Nickel Dust", "Nikkelstof"),
    ("Tin Dust", "Tinstof"),
    ("Raw Tin", "Ruw tin"),
    ("Raw Nickel", "Ruw nikkel"),
    ("Tin Ingot", "Tinstaaf"),
    ("Nickel Ingot", "Nikkelstaaf"),
    ("Steel Ingot", "Staalstaaf"),
    ("Steel Nugget", "Staalklompje"),
    ("Tin Nugget", "Tinklompje"),
    ("Nickel Nugget", "Nikkelklompje"),
    ("Steel Block", "Staalblok"),
    ("Tin Block", "Tinblok"),
    ("Nickel Block", "Nikkelblok"),
    ("Chromite Ore", "Chromieterts"),
    ("Graphite Ore", "Grafieterts"),
    ("Tin Ore", "Tinerts"),
    ("Nickel Ore", "Nikkelerts"),
    ("Process failed", "Proces mislukt"),
    ("Stored Fluid", "Opgeslagen vloeistof"),
    ("Empty", "Leeg"),
    ("Any order", "Willekeurige volgorde"),
    ("Machine", "Machine"),
    ("Layers (top -> bottom)", "Lagen (boven -> onder)"),
    ("Click a chapter.", "Klik op een hoofdstuk."),
    ("Index", "Inhoud"),
    ("Next", "Volgende"),
    ("Prev", "Vorige"),
    ("PC Survival Manual", "PC-overlevingsgids"),
    ("How to build a PC from scratch in survival", "Hoe je in survival een PC vanaf nul bouwt"),
    ("Time:", "Tijd:"),
    ("Energy:", "Energie:"),
    ("Output:", "Uitvoer:"),
    ("Creosote:", "Creosoot:"),
    ("Size:", "Afmeting:"),
    ("Multiblock: Formed", "Multiblok: Compleet"),
    ("Multiblock: Incomplete", "Multiblok: Onvolledig"),
    ("Progress:", "Voortgang:"),
    ("Fuel burn:", "Brandstof:"),
    ("Electrode:", "Elektrode:"),
    ("VM: Running", "VM: Actief"),
    ("VM: Off", "VM: Uit"),
    ("Generator: Burning", "Generator: Brandt"),
    ("Generator: Idle", "Generator: Inactief"),
    ("Tech Revised", "Tech Revised"),
    ("Missing", "Ontbreekt"),
    ("structure is valid", "structuur is geldig"),
    ("structure is incomplete", "structuur is onvolledig"),
    ("Structure hologram projected. Cyan = missing, green = placed, red = wrong block.",
     "Structuurhologram geprojecteerd. Cyaan = ontbreekt, groen = geplaatst, rood = verkeerd blok."),
    ("Not a fuel item", "Dit is geen brandstof"),
    ("Generator:", "Generator:"),
    ("Cannot auto-build: blocked at %s, %s, %s", "Kan niet autobouwen: geblokkeerd op %s, %s, %s"),
    ("Auto-build finished but structure is still incomplete.", "Autobouw klaar, maar de structuur is nog onvolledig."),
    ("Multiblock auto-built successfully.", "Multiblok succesvol autogebouwd."),
    ("Multiblock structure formed.", "Multiblokstructuur gevormd."),
    ("Multiblock structure is incomplete.", "Multiblokstructuur is onvolledig."),
    ("You need operator permission to use the VM workstation.", "Je hebt operatorrechten nodig om het VM-werkstation te gebruiken."),
    ("VM configuration saved.", "VM-configuratie opgeslagen."),
    ("VBoxManage not found. Expected at %s", "VBoxManage niet gevonden. Verwacht op %s"),
    ("VM command error: %s", "VM-commando fout: %s"),
    ("VM command failed (exit %s): %s", "VM-commando mislukt (exit %s): %s"),
    ("VM %s is already running.", "VM %s draait al."),
    ("VM %s is locked.", "VM %s is vergrendeld."),
    ("VM %s started.", "VM %s gestart."),
    ("Starting VM %s...", "VM %s starten..."),
    ("VM %s stopped.", "VM %s gestopt."),
    ("Stopping VM %s...", "VM %s stoppen..."),
    ("Chair is missing.", "Stoel ontbreekt."),
    ("Desk surface piece missing.", "Bureaubladstuk ontbreekt."),
    ("Transport frame legs missing.", "Transportframepoten ontbreken."),
    ("Missing HDMI cable.", "HDMI-kabel ontbreekt."),
    ("Missing keyboard.", "Toetsenbord ontbreekt."),
    ("Missing mouse.", "Muis ontbreekt."),
    ("Missing power cable.", "Stroomkabel ontbreekt."),
    ("Missing speaker.", "Luidspreker ontbreekt."),
    ("Missing desktop tower.", "Desktoptoren ontbreekt."),
    ("Missing Drill Head.", "Boorkop ontbreekt."),
    ("Missing Energy Input Hatch.", "Energie-invoerluik ontbreekt."),
    ("Missing Fluid Input Bus.", "Vloeistofinvoerbus ontbreekt."),
    ("Missing Fluid Output Bus.", "Vloeistofuitvoerbus ontbreekt."),
    ("Missing Input Bus.", "Invoerbus ontbreekt."),
    ("Missing Output Bus.", "Uitvoerbus ontbreekt."),
    ("Missing Electric Arc Furnace Heater.", "Vlamboogovenverwarming ontbreekt."),
    ("Missing Hydrogen Output Bus.", "Waterstofuitvoerbus ontbreekt."),
    ("Missing Oxygen Output Bus.", "Zuurstofuitvoerbus ontbreekt."),
    ("Close", "Sluiten"),
    ("Exit Full", "Volledig uit"),
    ("Fullscreen", "Volledig scherm"),
    ("Refresh", "Vernieuwen"),
    ("Save", "Opslaan"),
    ("Start Command", "Startcommando"),
    ("Stop Command", "Stopcommando"),
    ("VM Name", "VM-naam"),
    ("Preview", "Voorbeeld"),
    ("Click preview to focus", "Klik op voorbeeld om te focussen"),
    ("Connecting...", "Verbinden..."),
    ("Failed to decode preview frame.", "Voorbeeldsframe decoderen mislukt."),
    ("F11: fullscreen preview", "F11: volledig voorbeeld"),
    ("Preview focused", "Voorbeeld gefocust"),
    ("Press F11 to exit fullscreen.", "Druk op F11 om volledig scherm te verlaten."),
    ("Mouse click sent to VM.", "Muisklik naar VM gestuurd."),
    ("Mouse click unavailable.", "Muisklik niet beschikbaar."),
    ("Windows 10 VM Config", "Windows 10 VM-configuratie"),
    ("All layers", "Alle lagen"),
    ("Layers", "Lagen"),
    ("Next layer", "Volgende laag"),
    ("Previous layer", "Vorige laag"),
    ("Local coordinates", "Lokale coördinaten"),
    ("Required Parts", "Benodigde onderdelen"),
    ("Click a block in the preview.", "Klik op een blok in het voorbeeld."),
    ("Scroll to zoom.", "Scroll om te zoomen."),
    ("Zoom in", "Inzoomen"),
    ("Zoom out", "Uitzoomen"),
    ("PC Workstation", "PC-werkstation"),
    ("Bucket", "Emmer"),
    ("Ingot", "Staaf"),
    ("Nugget", "Klompje"),
    ("Dust", "Stof"),
    ("Ore", "Erts"),
    ("Block", "Blok"),
    ("Controller", "Besturing"),
    ("Frame", "Frame"),
    ("Tester", "Tester"),
    ("Assembler", "Assembler"),
    ("Printer", "Printer"),
    ("Oven", "Oven"),
    ("Press", "Pers"),
    ("Station", "Station"),
    ("Cable", "Kabel"),
    ("Wire", "Draad"),
    ("Sheet", "Plaat"),
    ("Pellets", "Pellets"),
    ("Package", "Behuizing"),
    ("Board", "Bord"),
    ("Glass", "Glas"),
    ("Monitor", "Monitor"),
    ("Keyboard", "Toetsenbord"),
    ("Mouse", "Muis"),
    ("Speaker", "Luidspreker"),
    ("Assembled", "Gemonteerd"),
    ("Tested", "Getest"),
    ("Programmed", "Geprogrammeerd"),
    ("Finished", "Afgewerkt"),
    ("Drilled", "Geboord"),
    ("Masked", "Gemaskeerd"),
    ("Routed", "Gefreesd"),
    ("Placed", "Geplaatst"),
    ("Reflowed", "Gesoldeerd"),
    ("Coated", "Gecoat"),
    ("Sealed", "Verzegeld"),
    ("Jacketed", "Ommanteld"),
    ("Shielded", "Afgeschermd"),
    ("Twisted Pair", "Getwist paar"),
    ("Copper", "Koper"),
    ("Steel", "Staal"),
    ("Iron", "IJzer"),
    ("Gold", "Goud"),
    ("Water", "Water"),
    ("Hydrogen", "Waterstof"),
    ("Oxygen", "Zuurstof"),
    ("Chlorine", "Chloor"),
    ("Ethylene", "Etheen"),
    ("Naphtha", "Nafta"),
    ("Creosote", "Creosoot"),
    ("Fuel", "Brandstof"),
    ("uses", "gebruiken"),
    ("left", "over"),
    ("ticks", "ticks"),
    ("Arc Power", "Boogkracht"),
    ("Technology Revised", "Technology Revised"),
    ("Rock Eater", "Rotsvreter"),
    ("Let There Be FE", "Er zij FE"),
    ("Charring", "Verscholen"),
    ("The Age of Steel", "Het stalen tijdperk"),
    ("Ferrite Chrome", "Ferrochroom"),
    ("Stainless Age", "Roestvast tijdperk"),
    ("Sweet Tin", "Zoet tin"),
    ("Circuit Smith", "Circuit-smid"),
    ("Blank Mind", "Lege geest"),
    ("Silicon Valley", "Silicon Valley"),
    ("It Computes", "Het rekent"),
    ("Desk Job", "Bureaubaan"),
    ("It's Alive!", "Het leeft!"),
]

KEEP = {
    "PCB", "PCBA", "CPU", "GPU", "PSU", "HDD", "SSD", "HDMI", "LCD", "LED", "AOI", "AOD",
    "EAF", "CNC", "SMT", "ICT", "IHS", "MCU", "NAND", "DRAM", "DIMM", "IEC", "PVC", "ABS",
    "VCM", "FR-4", "FR4", "UV", "LDI", "FE", "mB", "VM", "JEI", "CAD", "SPD", "EEPROM",
    "VRAM", "HEPA", "RAM", "IC", "GUI",
}


def translate(text):
    if not isinstance(text, str):
        return text
    out = text
    for src, dst in GLOSSARY:
        out = out.replace(src, dst)
    return out


def main():
    with open(EN, encoding="utf-8") as f:
        en = json.load(f)
    en.update(EXTRA)
    for cid, (title, body) in CHAPTERS.items():
        en[f"screen.tech_revised.manual.chapter.{cid}.title"] = title
        en[f"screen.tech_revised.manual.chapter.{cid}.body"] = body.replace("\r\n", "\n")
    with open(EN, "w", encoding="utf-8") as f:
        json.dump(en, f, indent=2, ensure_ascii=False)
        f.write("\n")

    nl = {k: translate(v) for k, v in en.items()}
    # dedicated Dutch manual titles
    nl_titles = {
        "the_goal": "Het doel",
        "factory_rules": "Fabrieksregels",
        "configurator": "Configurator",
        "first_tools": "Eerste gereedschap",
        "ore_crushing": "Erts en breken",
        "mining_budget": "Mijnbudget",
        "solid_fuel_generator": "Vastebrandstofgenerator",
        "coke_oven": "Cokesoven",
        "steel": "Staal",
        "stainless": "Roestvast staal",
        "crude_oil": "Ruwe olie",
        "plastics": "Kunststoffen",
        "copper_wire": "Koperdraad",
        "fiberglass": "Glasvezel",
        "bare_pcbs": "Kale PCB's",
        "bare_pcbs_2": "Kale PCB's 2",
        "silicon_ics": "Silicium-IC's",
        "ic_packages": "IC-behuizingen",
        "ram": "RAM",
        "motherboard": "Moederbord",
        "cpu": "CPU",
        "gpu": "GPU",
        "psu": "PSU",
        "hdd": "HDD",
        "ssd": "SSD",
        "case": "Behuizing",
        "tower_assembly": "Torenassemblage",
        "monitor": "Monitor",
        "keyboard": "Toetsenbord",
        "mouse": "Muis",
        "hdmi_cable": "HDMI-kabel",
        "power_cable": "Stroomkabel",
        "speakers": "Luidsprekers",
        "desk_chair": "Bureau en stoel",
        "workstation": "Werkstation",
    }
    for cid, title in nl_titles.items():
        nl[f"screen.tech_revised.manual.chapter.{cid}.title"] = title
    with open(NL, "w", encoding="utf-8") as f:
        json.dump(nl, f, indent=2, ensure_ascii=False)
        f.write("\n")
    print(f"en_us keys={len(en)} nl_nl keys={len(nl)}")


if __name__ == "__main__":
    main()
