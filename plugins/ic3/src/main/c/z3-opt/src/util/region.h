/*++
Copyright (c) 2006 Microsoft Corporation

Module Name:

    region.h

Abstract:

    Region/Arena memory manager

Author:

    Leonardo de Moura (leonardo) 2006-09-13.

Revision History:

--*/
#ifndef _REGION_H_
#define _REGION_H_
#include<cstdlib>
#include<iostream>

#ifdef Z3DEBUG

#include"vector.h"

class region {
    ptr_vector<char> m_chuncks;
    unsigned_vector  m_scopes;
public:
    ~region() {
        reset();
    }

    void * allocate(size_t size) {
        char * r = alloc_svect(char, size);
        m_chuncks.push_back(r);
        return r;
    }

    void reset() {
        ptr_vector<char>::iterator it  = m_chuncks.begin();
        ptr_vector<char>::iterator end = m_chuncks.end();
        for (; it != end; ++it) {
            dealloc_svect(*it);
        }
        m_chuncks.reset();
        m_scopes.reset();
    }

    void push_scope() {
        m_scopes.push_back(m_chuncks.size());
    }


    void pop_scope() {
        unsigned old_size = m_scopes.back();
        m_scopes.pop_back();
        ptr_vector<char>::iterator it  = m_chuncks.begin() + old_size;
        ptr_vector<char>::iterator end = m_chuncks.end();
        for (; it != end; ++it) {
            dealloc_svect(*it);
        }
        m_chuncks.shrink(old_size);
    }
    
    void pop_scope(unsigned num_scopes) {
        for (unsigned i = 0; i < num_scopes; i++) {
            pop_scope();
        }
    }

    void display_mem_stats(std::ostream & out) const;
};

#else

/**
\brief Implement explicit region memory manager.
*/
class region {
    struct mark {
        char * m_curr_page;
        char * m_curr_ptr;
        mark * m_prev_mark;
        mark(char * page, char * ptr, mark * m):m_curr_page(page), m_curr_ptr(ptr), m_prev_mark(m) {}
    };
    char *   m_curr_page;
    char *   m_curr_ptr;     //!< Next free space in the current page.
    char *   m_curr_end_ptr; //!< Point to the end of the current page.
    char *   m_free_pages;
    mark *   m_mark;
    void allocate_page();
    void recycle_curr_page();
public:
    region();
    ~region();
    void * allocate(size_t size);
    void reset();
    void push_scope();
    void pop_scope();
    void pop_scope(unsigned num_scopes) {
        for (unsigned i = 0; i < num_scopes; i++) {
            pop_scope();
        }
    }
    void display_mem_stats(std::ostream & out) const;
};

#endif

inline void * operator new(size_t s, region & r) { return r.allocate(s); }

inline void * operator new[](size_t s, region & r) { return r.allocate(s); }

inline void operator delete(void *, region & ) { /* do nothing */ }

inline void operator delete[](void *, region & ) { /* do nothing */ }

#endif /* _REGION_H_ */

