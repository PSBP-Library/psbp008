package psbp.implementation.program.reactive

import psbp.specification.program.{Program}

import psbp.implementation.program.{program}

import psbp.implementation.computation.reactive.{Reactive, `=>R`}

import psbp.implementation.computation.reactive.{reactiveComputation}

private[psbp] given reactiveProgram[
    &&[+_, +_]: psbp.specification.Product,
    ||[+_, +_]: psbp.specification.Sum
]: Program[`=>R`, &&, ||] = program[Reactive, &&, ||]
