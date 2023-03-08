# Program Specification Based Programming

This is lesson 008 of a "Program Specification Based Programming" course.

All comments are welcome at luc.duponcheel[at]gmail.com.

## Introduction

In lesson007 we have defined all `trait Program` members in terms of type `Active`, type `Tuple2` and type `Either` as a 
`given`.

We have also defined all `trait Program` members in terms of type `Reactive`, type `CaseProduct` and type `EnumSum` as a
`given`.

Recall that, for now, the function level `Product` and function level `Sum` implementations are not really different.

## Content

### `ParallelComposition`

Below the algorithm related program concept `ParallelComposition` is specified.

```scala
package psbp.specification.algorithm

import psbp.specification.function.{Function}

import psbp.specification.dataStructure.{Product}

trait ParallelComposition[
    >-->[-_, +_]: [>-->[-_, +_]] =>> Function[>-->, &&]: SequentialComposition: [>-->[
        -_,
        +_
    ]] =>> Product[>-->, &&],
    &&[+_, +_]
]:

  private lazy val summonedFunction = summon[Function[>-->, &&]]

  import summonedFunction.{`z>-->z`}

  private lazy val summonedProduct = summon[Product[>-->, &&]]

  import summonedProduct.{`(z&&y)>-->y`, `z>-->(z&&z)`}

  // external defined

  extension [Z, Y, X, W](`z>-->x`: Z >--> X)
    def |&&&|(`y>-->w`: Y >--> W): (Z && Y) >--> (X && W) =
      parallelComposition(`z>-->x`, `y>-->w`)

  extension [Z, Y, X](`z>-->y`: Z >--> Y)
    def |&&|(`z>-->x`: Z >--> X): Z >--> (Y && X) =
      `z>-->(z&&z)` >--> (`z>-->y` |&&&| `z>-->x`)

  def async[Z, Y](`z>-->y`: Z >--> Y): Z >--> Y =
    (`z>-->z` && `z>-->y`) >--> `(z&&y)>-->y`

  // internal declared

  private[psbp] def parallelComposition[Z, Y, X, W](
      `z>-->x`: Z >--> X,
      `y>-->w`: Y >--> W
  ): (Z && Y) >--> (X && W)
```

### Updating `Product`

Member `|&&|` of `ParallelComposition` uses member `` `z>-->(z&&z)` `` of `Product` and therefore `Product` needs to be
updated accordingly.

```scala
package psbp.specification.dataStructure

import psbp.specification.function.{Function}

import psbp.specification.algorithm.{SequentialComposition}

private[psbp] trait Product[
    >-->[-_, +_]: SequentialComposition: [>-->[-_, +_]] =>> Function[>-->, &&],
    &&[+_, +_]: psbp.specification.Product
]:

  private lazy val summonedFunction = summon[Function[>-->, &&]]

  import summonedFunction.{functionLift, `z>-->z`}

  private lazy val summonedProduct = summon[psbp.specification.Product[&&]]

  import summonedProduct.{`(z&&y)=>z`, `(z&&y)=>y`, unfoldProduct}

  // external defined

  def `(z&&y)>-->z`[Z, Y]: (Z && Y) >--> Z = functionLift(`(z&&y)=>z`)

  def `(z&&y)>-->y`[Z, Y]: (Z && Y) >--> Y = functionLift(`(z&&y)=>y`)

  extension [Z, Y, X](`z>-->y`: Z >--> Y)
    def &&(`z>-->x`: => Z >--> X): Z >--> (Y && X) = product(`z>-->y`, `z>-->x`)

  extension [Z, Y, X, W](`z>-->x`: Z >--> X)
    def &&&(`y>-->w`: => Y >--> W): (Z && Y) >--> (X && W) =
      (`(z&&y)>-->z` >--> `z>-->x`) && (`(z&&y)>-->y` >--> `y>-->w`)

  def `(z&&y&&x)>-->z`[Z, Y, X]: (Z && Y && X) >--> Z =
    `(z&&y)>-->z` >--> `(z&&y)>-->z`

  def `(z&&y&&x)>-->y`[Z, Y, X]: (Z && Y && X) >--> Y =
    `(z&&y)>-->z` >--> `(z&&y)>-->y`

  def `(z&&y&&x)>-->x`[Z, Y, X]: (Z && Y && X) >--> X =
    `(z&&y)>-->y`

  def `(z&&y&&x)>-->(y&&x)`[Z, Y, X]: (Z && Y && X) >--> (Y && X) =
    `(z&&y&&x)>-->y` && `(z&&y)>-->y`

  def `(z&&y&&x)>-->(z&&x)`[Z, Y, X]: (Z && Y && X) >--> (Z && X) =
    `(z&&y&&x)>-->z` && `(z&&y)>-->y`

  def `(z&&y&&x)>-->(z&&y)`[Z, Y, X]: (Z && Y && X) >--> (Z && Y) =
    `(z&&y&&x)>-->z` && `(z&&y&&x)>-->y`

  // ...

  // internal declared

  private[psbp] def product[Z, Y, X](
      `z>-->y`: Z >--> Y,
      `z>-->x`: => Z >--> X
  ): Z >--> (Y && X)

  // internal defined

  private[psbp] def `z>-->(z&&z)`[Z]: Z >--> (Z && Z) = 
    `z>-->z` && `z>-->z`
```

### Updating `Algorithm`

```scala
package psbp.specification.algorithm

private[psbp] trait Algorithm[>-->[-_, +_], &&[+_, +_], ||[+_, +_]]
    extends IfThenElse[>-->, &&, ||],
      LocalDefinition[>-->, &&],
      SequentialComposition[>-->],
      ParallelComposition[>-->, &&]
```

### For now implementing `ParallelComposition` in a trivial way

For now we implement `ParallelComposition` in a trivial way to keep the `Scala` compiler happy.

```scala
package psbp.implementation.algorithm

import psbp.specification.computation.{Computation}

private[psbp] trait ParallelComposition[C[+_]: Computation, &&[+_, +_]]
    extends psbp.specification.algorithm.ParallelComposition[[Z, Y] =>> Z => C[Y], &&]:

  // internal declared

  private[psbp] def parallelComposition[Z, Y, X, W](
      `z>-->x`: Z => C[X],
      `y>-->w`: Y => C[W]
  ): (Z && Y) => C[X && W] = ???
```

### Updating `Program` implementation in term of `Computation`, function level `Product` and function level `Sum`.

The implementation of `Program` in term of `Computation`, function level `Product` and function level `Sum`needs to be
updated accordingly.

```scala
package psbp.implementation.program

import psbp.specification.computation.{Computation}

import psbp.implementation.function.{Function}

import psbp.implementation.algorithm.{SequentialComposition, ParallelComposition}

import psbp.implementation.dataStructure.{DataStructure}

private[psbp] given program[
    C[+_]: Computation,
    &&[+_, +_]: psbp.specification.Product,
    ||[+_, +_]: psbp.specification.Sum
]: psbp.specification.program.Program[[Z, Y] =>> Z => C[Y], &&, ||]
  with Function[C, &&]
  with SequentialComposition[C]
  with ParallelComposition[C, &&]
  with DataStructure[C, &&, ||]
```

### `parallelFibonacciProgram`

The code below defines `parallelFibonacciProgram` using the program concepts specified so far.

```scala
package examples.specification

import psbp.specification.program.{Program}

import examples.specification.{
  isZeroProgram,
  oneProgram,
  isOneProgram,
  subtractOneProgram,
  subtractTwoProgram,
  addProgram
}

def parallelFibonacciProgram[
    >-->[-_, +_]: [_[-_, +_]] =>> Program[>-->, &&, ||],
    &&[+_, +_],
    ||[+_, +_]
]: BigInt >--> BigInt =

  lazy val summonedProgram: Program[>-->, &&, ||] = summon[Program[>-->, &&, ||]]

  import summonedProgram.{Let, If, &&, &&&}

  If(isZeroProgram) Then {
    oneProgram
  } Else {
    If(isOneProgram) Then {
      oneProgram
    } Else {
      (subtractOneProgram && subtractTwoProgram) >-->
        (fibonacciProgram03 |&&&| fibonacciProgram03) >-->
        addProgram
    }
  }
```

## Conclusion

We have specified and, for now, trivially implemented the `ParallelComposition` program concept and defined
`parallelFibonacci` using it.




