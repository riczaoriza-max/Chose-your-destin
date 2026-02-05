# Implementation notes

## Choice cycle

- The cycle is controlled by `ChoiceManager.tick()`, usually surfaced by `ChoiceRuntime`.
- Time is tracked in seconds (`getGameTimeSeconds()`) to remain tick-rate independent.
- When the timer reaches zero, the current effect is removed and a new choice is opened.

## Configuration loading

- `ChoiceConfigLoader` uses Gson to load the bundled `src/common/resources/choose_your_destiny/choices.json`.
- `ChoiceRuntime.fromConfig(...)` normalizes the default duration when a choice omits `durationSeconds`.

## Boss bar

- The boss bar text uses the prompt from the active choice.
- Progress is `remainingTime / duration`.
- When swapping choices, the previous boss bar is cleared.

## Choice menu

- The menu displays the `prompt` and two options, always drawn from the pool of 100+ choices.
- When a player selects an option, call `ChoiceManager.selectOption(player, index)`.
- The menu should block multiple selections for the same event.

## Effect mapping

Effects are defined in `choices.json` (bundled under `choose_your_destiny/` and copied to config) using string keys. For the real implementation:

- Map effects to concrete attributes (speed, damage, mining, hunger, etc.).
- Use attributes or potion effects as needed.
- Ensure effects are removed cleanly when the cycle ends.
