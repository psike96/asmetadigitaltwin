asm mxchip

import StandardLibrary

signature:
	enum domain Status = {LOW | MID | HIGH}
	controlled text: Status
	controlled led: Boolean
	monitored temperature: Real

definitions:
	main rule r_Main =
		if temperature < 45.0 then
			par
				text := LOW
				led := false
			endpar
		else
			if temperature > 75.0 then
				par
					text := HIGH
					led := true
				endpar
			else
				par
					text := MID
					led := false
				endpar
			endif
		endif

default init s0:
	function text = MID
	function led = false
