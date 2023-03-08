# Program Specification Based Programming

This is lesson 007 of a "Program Specification Based Programming" course.

All comments are welcome at luc.duponcheel[at]gmail.com.

## Introduction

In lesson006 we have defined all `trait Program` members in terms of `trait Computation` members, function level
`trait Product` members and function level `trait Sum` members as a `given`.

## Content

### Implementing `Computation` in terms of type `Active`

Let

```scala
package psbp.implementation.computation.active

private[psbp] type Active = [Y] =>> Y

private[psbp] type `=>A` = [Z, Y] =>> Z => Active[Y]
```

in

```scala
package psbp.implementation.computation.active

import psbp.specification.computation.{Computation}

private[psbp] given activeComputation: Computation[Active] with

  private[psbp] override def expressionLift[Z]: Z `=>A` Z = z => z

  private[psbp] override def bind[Z, Y]: Active[Z] => (Z `=>A` Y) => Active[Y] = `a[z]` =>
    `z=>a[y]` => `z=>a[y]`(`a[z]`)
```

### Implementing `Computation` in terms of type `Reactive`

Let

```scala
package psbp.implementation.computation.reactive

private[psbp] type Reactive = [Y] =>> (Y => Unit) => Unit

private[psbp] type `=>R` = [Z, Y] =>> Z => Reactive[Y]
```

in

```scala
package psbp.implementation.computation.reactive

import psbp.specification.computation.{Computation}

private[psbp] given reactiveComputation: Computation[Reactive] with

  private[psbp] override def expressionLift[Z]: Z `=>R` Z = z => `z=>u` => `z=>u`(z)

  private[psbp] override def bind[Z, Y]: Reactive[Z] => (Z `=>R` Y) => Reactive[Y] =
    `r[z]` => `z=>r[y]` => `y=>u` => `r[z]` { z => `z=>r[y]`(z)(`y=>u`) }
```

### Implementing `Program` in terms of type `Active`, function level `Product` and function level `Sum`

```scala
package psbp.implementation.program.active

import psbp.specification.program.{Program}

import psbp.implementation.program.{program}

import psbp.implementation.computation.active.{Active, `=>A`}

import psbp.implementation.computation.active.{activeComputation}

private[psbp] given activeProgram[
    &&[+_, +_]: psbp.specification.Product,
    ||[+_, +_]: psbp.specification.Sum
]: Program[`=>A`, &&, ||] = program[Active, &&, ||]
```

### Implementing `Program` in terms of type `Reactive`, function level `Product` and function level `Sum`

```scala
package psbp.implementation.program.reactive

import psbp.specification.program.{Program}

import psbp.implementation.program.{program}

import psbp.implementation.computation.reactive.{Reactive, `=>R`}

import psbp.implementation.computation.reactive.{reactiveComputation}

private[psbp] given reactiveProgram[
    &&[+_, +_]: psbp.specification.Product,
    ||[+_, +_]: psbp.specification.Sum
]: Program[`=>R`, &&, ||] = program[Reactive, &&, ||]
```

### Implementing function level `Product` in terms of type `Tuple2`

```scala
package psbp.implementation

import psbp.specification.{Product}

private[psbp] given tuple2Product: Product[Tuple2] with

  private[psbp] override def `(z&&y)=>y`[Z, Y]: Tuple2[Z, Y] => Y = { case (z, y) => y }

  private[psbp] override def `(z&&y)=>z`[Z, Y]: Tuple2[Z, Y] => Z = { case (z, y) => z }

  private[psbp] override def unfoldProduct[Z, Y, X]
      : (Z => Y) => (Z => X) => Z => Tuple2[Y, X] =
    `z=>y` => `z=>x` => z => (`z=>y`(z), `z=>x`(z))
```

### Implementing function level `Sum` in terms of type `Either`

```scala
package psbp.implementation

import psbp.specification.{Sum}

private[psbp] given eitherSum: Sum[Either] with

  private[psbp] override def `y=>(y||z)`[Z, Y]: Y => Either[Y, Z] = z => Left(z)

  private[psbp] override def `z=>(y||z)`[Z, Y]: Z => Either[Y, Z] = y => Right(y)

  private[psbp] override def foldSum[Z, Y, X]: (X => Z) => (Y => Z) => Either[X, Y] => Z =
    `x=>z` =>
      `y=>z` =>
        case Left(x) =>
          `x=>z`(x)
        case Right(y) =>
          `y=>z`(y)
```

### Implementing `Program` in terms of type `Active`, type `Tuple2` and type `Either`

```scala
package psbp.implementation.program.active.tuple2.either

import psbp.specification.program.{Program}

import psbp.implementation.program.active.{activeProgram}

import psbp.implementation.{tuple2Product, eitherSum}

import psbp.implementation.computation.active.{`=>A`}

private[psbp] given activeTuple2EitherProgram: Program[`=>A`, Tuple2, Either] =
  activeProgram[Tuple2, Either]
```

### Implementing function level `Product` in terms of type `CaseProduct`

```scala
package psbp.implementation

import psbp.specification.{Product}

case class CaseProduct[+Z, +Y](z: Z, y: Y)

private[psbp] given caseProduct: Product[CaseProduct] with

  private[psbp] override def `(z&&y)=>z`[Z, Y]: CaseProduct[Z, Y] => Z =
    case CaseProduct(z, y) => z

  private[psbp] override def `(z&&y)=>y`[Z, Y]: CaseProduct[Z, Y] => Y =
    case CaseProduct(z, y) => y

  private[psbp] override def unfoldProduct[Z, Y, X]
      : (Z => Y) => (Z => X) => Z => CaseProduct[Y, X] =
    `z=>y` => `z=>x` => z => CaseProduct(`z=>y`(z), `z=>x`(z))
```

For now the function level `Product` implementations are not really different.

### Implementing function level `Sum` in terms of type `EnumSum`

```scala
package psbp.implementation

import psbp.specification.{Sum}

enum EnumSum[+Z, +Y] {
  case Left(z: Z) extends EnumSum[Z, Y]
  case Right(y: Y) extends EnumSum[Z, Y]
}

import EnumSum.{Left, Right}

private[psbp] given enumSum: Sum[EnumSum] with

  private[psbp] override def `y=>(y||z)`[Z, Y]: Y => EnumSum[Y, Z] = z => Left(z)

  private[psbp] override def `z=>(y||z)`[Z, Y]: Z => EnumSum[Y, Z] = y => Right(y)

  private[psbp] override def foldSum[Z, Y, X]
      : (X => Z) => (Y => Z) => EnumSum[X, Y] => Z =
    `x=>z` =>
      `y=>z` =>
        case Left(x) =>
          `x=>z`(x)
        case Right(y) =>
          `y=>z`(y)
```

For now the function level `ProductSum` implementations are not really different.

### Implementing `Program` in terms of type `Reactive`, type `CaseProduct` and type `EnumSum`

```scala
package psbp.implementation.program.reactive.tuple2.either

import psbp.specification.program.{Program}

import psbp.implementation.program.reactive.{reactiveProgram}

import psbp.implementation.{CaseProduct, caseProduct, EnumSum, enumSum}

import psbp.implementation.computation.reactive.{`=>R`}

private[psbp] given reactiveCaseProductEnumSumProgram
    : Program[`=>R`, CaseProduct, EnumSum] =
  reactiveProgram[CaseProduct, EnumSum]
```

## Conclusion

We have defined all `trait Program` members in terms of type `Active`, type `Tuple2` and type `Either` as a `given`.

We have also defined all `trait Program` members in terms of type `Reactive`, type `CaseProduct` and type `EnumSum` as a
`given`.

For now the function level `Product` and function level `Sum` implementations are not really different.

