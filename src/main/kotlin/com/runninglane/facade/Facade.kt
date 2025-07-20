package com.runninglane.facade

/**
 * Interface representing a facade that provides access to its delegate object
 * @param D The type of the delegate
 */
interface Facade<D> {
    /**
     * The delegate object used by this facade
     */
    val delegate: D
}
