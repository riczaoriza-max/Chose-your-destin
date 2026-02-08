# Choose Your Destiny (v1)

**Author:** kotoritos  
**License:** ARR (All Rights Reserved)  
**Version:** v1  
**Target compatibility:** Minecraft 1.20.1 (Fabric)

## Overview

This mod creates a system of temporary choices that directly affect gameplay. Each event presents the player with a question and two options (for example: “jump higher but take double fall damage” or “mine 2x faster but get fewer resources”) shown in an **on-screen menu**. After choosing, the effect lasts **3 minutes**, and the remaining time is shown in a **boss bar**. When the timer ends, a new choice appears in the menu.

## Main rules

- Each event lasts **180 seconds (3 minutes)**.
- The remaining effect time is shown in a **boss bar**.
- The menu always shows **only two options**, drawn from **over 100** different choices.
- When the timer ends, the effect expires and **a new choice** is presented.
- Choices are **mutually exclusive** per event (one active option at a time).

## Architecture

This repository provides a Fabric project with shared logic in `common` and a Fabric entrypoint in `fabric`.

- `src/common/java/...`: shared logic (choices, timer, boss bar, effects).
- `src/fabric/java/...`: Fabric entrypoint to register events and load shared logic.
- `src/common/resources/choose_your_destiny/choices.json`: bundled default data file with prompts and options (100+).
- `config/choose_your_destiny/choices.json`: runtime override written/read from the Minecraft config directory.
- `ChoiceConfigLoader`: Gson-based data loading (available in the Minecraft runtime).

## Data content

`choices.json` defines the questions and options (bundled under `choose_your_destiny/` and copied to the config directory on first run). Each choice includes:

- `id`: identifier.
- `prompt`: text shown to the player.
- `options`: two options, each with `label` and `effects`.
- `durationSeconds`: effect duration (fixed at 180, but configurable).

## Notes

This project ships the full runtime hooks, UI, effect mapping, and Fabric build setup.
