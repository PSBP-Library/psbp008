package psbp.implementation.program.active

import psbp.specification.program.{Program}

import psbp.implementation.program.{program}

import psbp.implementation.computation.active.{Active, `=>A`}

import psbp.implementation.computation.active.{activeComputation}

private[psbp] given activeProgram[
    &&[+_, +_]: psbp.specification.Product,
    ||[+_, +_]: psbp.specification.Sum
]: Program[`=>A`, &&, ||] = program[Active, &&, ||]
