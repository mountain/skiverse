# Skiverse: A SKI universe

How did life began? This is one of the biggest unanswered question of science, but is very hard to reproduce and verify.
On the other hand, in computer science, [Von Neumann universal constructor](https://en.wikipedia.org/wiki/Von_Neumann_universal_constructor),
as a self-replicating construction, is not very complex, comparing thier biological counterpart.
This lead us to investigate the idea of ideal gas of programs.

Thousands of programs flying inside a box; when they collide, a new program is generated. By a computer science perspective,
we use gas dynamics and generation rules to replace Chaitin's random sample in AIT; by a biology perspective, we replace
chemical reactions to program dynamics.

## The idea

### logic rules

We choose a modified version of SKI as the basis of the program world for several reasons.
* SKI is Turing complete.
* Self-replicating program is quite simple in SKI.
* The construction of SKI program is easy which makes the marriage of ideal gas simpler.

The modified part of SKI is to introduce potential into the system to avoid executions of infinite recursion.
* when a SKI evaluation is not lead to mass/energy loss, the original SKI evaluation is hold.
* when a SKI evaluation is lead to mass/energy loss, especially with occurrence of the evaluation of combinator S,
 we must cost an amount of energy(potential) to conduct the evaluation.

### physical rules

Then we introduce the attribtes of mass, position, velocity, kinect, potential and momentum for each program and
related conservation laws, and apply the conservation laws on three process:
* collision: keep momentum conserved, convert some part of kinect into potential
* reaction: it is a SKI expression evaluation, accompanying mass generation/conversion from/to energy
* emission: convert energy(potential) into mass by exchange ratio of 1, and keep momentum conserved.
 
We use combinator S, K, I as materials, and combinator ι as purely energy just like photons.
 
### world setting

Just like earth, we receive short-wave radiation from sun to feed energy into the system,
 and emit long-wave radiation into the universe.

We introduce two world settings to make the system dissipative:
* ι-feeding: feed combinator ι into the system with a constant rate
* ι-escaping: combinator ι can escape from the system randomly


## Conclusions

WIP

## Acknowledgment

Thanks to Weiyi Qiu and Dong Wang for their discussions and encourages.

Thanks to Patryk Nusbaum for his unlicensed project [dynamicaabbtree](https://github.com/pateman/dynamicaabbtree)




  




 






 




