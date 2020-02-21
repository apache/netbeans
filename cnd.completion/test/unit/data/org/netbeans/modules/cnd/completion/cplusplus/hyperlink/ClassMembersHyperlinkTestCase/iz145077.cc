/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

namespace iz145077 {
/////////////////////////////////////////////////////////////////////////
//
// Forward declarations.
//
/////////////////////////////////////////////////////////////////////////

class MediaObj;

class MediaObj {
public:
    class ScopeHold;
    class Persist;
    friend class Persist;

    class Persist {
      public:
        friend class ScopeHold;
        friend class MediaObj;

        Persist()
          : m_obj(0)
        {}

        Persist(Persist& p);
        ~Persist();

        Persist& operator=(MediaObj* obj);
        Persist& operator=(ScopeHold&);
        Persist& operator=(Persist&);

      private:  // private methods
        MediaObj* preserve();
        void replace(MediaObj* obj);

      private:  // private data
        MediaObj*               m_obj;
    };

    class ScopeHold {
      public:
        ScopeHold()                 { m_obj = 0; }
        ScopeHold(Persist& p)       { m_obj = p.preserve(); }
        ScopeHold(MediaObj* o)      { if (o != 0) o->preserve(); m_obj=o; }
        ~ScopeHold()                { if (m_obj != 0) m_obj->release(); }
        bool null()                 { return m_obj == 0; }
        bool valid()                { return ! null(); }
        MediaObj* get()             { return m_obj; }
        MediaObj* forget()          { MediaObj* o=m_obj; m_obj=0; return o; }
        MediaObj* operator->()      { return m_obj; }

        int release()
        {
            int count = (m_obj != 0 ? m_obj->release() : 0);
            m_obj = 0;
            return count;
        }

        ScopeHold& operator=(Persist& p)
        {
            MediaObj* newObj = p.preserve();
            if (m_obj != 0)
                m_obj->release();
            m_obj = newObj;
            return *this;
        }

        ScopeHold& operator=(MediaObj* newObj)
        {
            // we assume newObj will not go away
            if (newObj != 0)
                newObj->preserve();
            if (m_obj != 0)
                m_obj->release();
            m_obj = newObj;
            return *this;
        }

        ScopeHold(const ScopeHold& rhs)
        {
            if (this != &rhs) {
                m_obj = rhs.m_obj;
                if (m_obj != 0)
                    m_obj->preserve();
            }
        }

        ScopeHold& operator=(const ScopeHold& rhs)
        {
            // we can do this because rhs has an extra reference
            if (this != &rhs) {
                if (m_obj != 0)
                    m_obj->release();
                m_obj = rhs.m_obj;
                if (m_obj != 0)         //MD: rhs.m_obj==0 (e.g. file locked)
                    m_obj->preserve();
            }
            return *this;
        }

        MediaObj& operator*()
        {
            return *m_obj;
        }

      private:
        MediaObj* m_obj;
    };

public:
    // reference counters
    virtual int preserve();
    virtual int release();
};

class CClipFile {
private:
    MediaObj::Persist m_obj;

public:
    bool getTimecode();
};

bool CClipFile::getTimecode()
{
    MediaObj::ScopeHold hold(m_obj);

    if (hold.valid()) {
        return true;
    }
    CClipFile* a;
    function(a->m_obj ? a->m_obj : a->m_obj);
    return false;
}
void function(void* ) {

}

class BasicFileIO {
  public:
    struct ReadReq {
        void*   cb;         // specifies callback mechanism
        const void*         reqTag;     // specifies request-specific tag
    };
};

class BufferTypes {
public:
    typedef BasicFileIO::ReadReq ReadReq;
};

class IntBufferTypes : public BufferTypes {
public:
    class PendingReq {
    private:
        const void*   m_readCb;   // specifies callback mechanism
        const void*         m_readTag;  // specifies request-specific tag


    public:     // methods
        void workingSet(const ReadReq& req)
        {
            m_readCb = &req.cb;     // ide says ok
            m_readTag = req.reqTag; // ide says ok
        }
        void brokenSet(const ReadReq&);
    };
};

void IntBufferTypes::PendingReq::brokenSet(const ReadReq& req)
{
    m_readCb = &req.cb;     // ide says cb can't be resolved
    m_readTag = req.reqTag; // ide says reqTag can't be resolved
}
}