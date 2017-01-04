#include <stdlib.h>
#include "epmc_error.h"
#include "epmc_util.h"

__attribute__ ((visibility("default")))
epmc_error_t double_mdp_unbounded_jacobi(int relative, double precision,
        int numStates, int *stateBounds, int *nondetBounds, int *targets,
        double *weights, int min, double *values) {
    double optInitValue = min ? INFINITY : -INFINITY;
    double *presValues = values;
    double *nextValues = malloc(sizeof(double) * numStates);
    if (nextValues == NULL) {
        return OUT_OF_MEMORY;
    }
    double *allocated = nextValues;
    double maxDiff;
    int numIter = 0;
    do {
        numIter++;
        maxDiff = 0.0;
        for (int state = 0; state < numStates; state++) {
            double presStateProb = presValues[state];
            int stateFrom = stateBounds[state];
            int stateTo = stateBounds[state + 1];
            double nextStateProb = optInitValue;
            for (int nondetNr = stateFrom; nondetNr < stateTo; nondetNr++) {
                int nondetFrom = nondetBounds[nondetNr];
                int nondetTo = nondetBounds[nondetNr + 1];
                double choiceNextStateProb = 0.0;
                for (int stateSucc = nondetFrom; stateSucc < nondetTo; stateSucc++) {
                    double weight = weights[stateSucc];
                    int succState = targets[stateSucc];
                    double succStateProb = presValues[succState];
                    double weighted = weight * succStateProb;
                    choiceNextStateProb += weighted;
                }
                nextStateProb = fopt(min, nextStateProb, choiceNextStateProb);
            }
            double diff = fabs(nextStateProb - presValues[state]);
            if (relative && presValues[state] != 0.0) {
                diff /= presValues[state];
            }
            maxDiff = diff > maxDiff ? diff : maxDiff;
            nextValues[state] = nextStateProb;
        }
        double *swap = nextValues;
        nextValues = presValues;
        presValues = swap;
    } while (maxDiff > precision / 2);
    for (int state = 0; state < numStates; state++) {
        values[state] = presValues[state];
    }
    free(allocated);
    return SUCCESS;
}
